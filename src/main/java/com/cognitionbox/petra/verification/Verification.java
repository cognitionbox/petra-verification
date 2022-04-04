package com.cognitionbox.petra.verification;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.lang.step.PEdge;
import com.cognitionbox.petra.annotations.Infinite;
import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.lang.step.PGraph;
import com.cognitionbox.petra.verification.tasks.ProveKaseTask;
import com.cognitionbox.petra.verification.tasks.ProveViewSoundnessAndCompletenessTask;
import com.cognitionbox.petra.verification.tasks.VerificationTask;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.cognitionbox.petra.verification.Strings.ACCEPT;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Verification {

    private static List<VerificationTask> tasks;

    public VerificationTask getTask() {
        return task;
    }

    private VerificationTask task;
    public Verification(VerificationTask task) {
        this.task = task;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection tasks() {
        PetraProgram.parseSrcFiles();
        tasks = new ArrayList<>();
        for (CompilationUnitWithData cu : PetraProgram.all.values().stream().filter(c->
                c.getClazz().isInterface() &&
                !Consumer.class.isAssignableFrom(c.getClazz()) &&
                !c.getClazz().isAnnotationPresent(Primative.class)).collect(Collectors.toList())) {
            tasks.add(new ProveViewSoundnessAndCompletenessTask(cu));
        }
        PetraProgram.LOG.debug("views: "+PetraProgram.dataTypeInfoMap);

        for (CompilationUnitWithData cu : PetraProgram.all.values().stream().filter(c->c.getClazz().isAnnotationPresent(View.class)).collect(Collectors.toList())) {
            String term = cu.getCompilationUnit().toString();
            term = term.replaceAll("empty empty","i");
            CompilationUnit programTerm = new JavaParser().parse(term).getResult().get();
            cu.setCompilationUnit(programTerm);

            for (MethodDeclaration action : cu.getCompilationUnit()
                                    .getInterfaceByName(cu.getClazz().getSimpleName()).get()
                                    .getMethodsByParameterTypes(cu.getClazz())){
                if (!action.isAnnotationPresent(Edge.class)){
                    int count = 0;
                    for (Expression kase : action.getBody().get()
                            .asBlockStmt()
                            .getStatements()
                            .get(0)
                            .asExpressionStmt()
                            .getExpression()
                            .asMethodCallExpr()
                            .getArguments()){
                        if (count==0){
                            count++;
                            continue;
                        }
                        tasks.add(new ProveKaseTask(action.getName().asString(),count,kase,cu));
                        count++;
                    }
                }
            }
        }
        return tasks;
    }

    @Test
    public void test() throws ClassNotFoundException {
        if (task instanceof ProveKaseTask){
            ProveKaseTask proveKaseTask = (ProveKaseTask) task;
            PetraProgram.rewriteSingleJoinPar(proveKaseTask);
            PetraProgram.rewriteGraphKaseStepsToEdges(proveKaseTask);
            PetraProgram.rewriteGraphKaseJoinParStepsToEdges(proveKaseTask);
            PetraProgram.rewriteStepsWithForall(proveKaseTask);
            PetraProgram.rewriteGraphKaseSeperatedStepsToNonSeperatedSteps(proveKaseTask);
            PetraProgram.rewriteJoinForallParSteps(proveKaseTask);
            PetraProgram.rewriteGraphKaseJoinParsToSeq(proveKaseTask);
            PetraProgram.rewriteSingleParStepToSeqStep(proveKaseTask);
            if (PetraProgram.rewriteKase(proveKaseTask)){
                task.markPassed();
                assertTrue(true);
            } else {
                fail();
            }
        } else if(task instanceof ProveViewSoundnessAndCompletenessTask){
            ProveViewSoundnessAndCompletenessTask soundnessAndCompletenessTask = (ProveViewSoundnessAndCompletenessTask)task;
            ClassOrInterfaceDeclaration c = soundnessAndCompletenessTask.getView().getCompilationUnit()
                    .getInterfaceByName(soundnessAndCompletenessTask.getView().getClazz().getSimpleName()).get();
            boolean result =  PetraProgram.isViewSoundAndComplete(soundnessAndCompletenessTask.getView().getClazz(),c);
            if (!result){
                fail();
            } else {
                task.markPassed();
            }
        }
    }

    @AfterClass
    public static void after(){
        if (tasks.stream().allMatch(t->t.passed())){
            //PetraProgram.convertToControlledEnglish();
        }
    }

    protected static void setRoot(Class<?> root) {
        if (!root.isAnnotationPresent(View.class)){
            throw new UnsupportedOperationException();
        }
        PetraProgram.rootGraphName = root.getSimpleName();
        PetraProgram.entryPointPackageName = root.getPackage().getName();
    }
}
