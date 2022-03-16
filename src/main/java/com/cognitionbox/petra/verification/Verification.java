package com.cognitionbox.petra.verification;

import com.cognitionbox.petra.lang.step.PEdge;
import com.cognitionbox.petra.annotations.Infinite;
import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.lang.step.PGraph;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Verification {
    public interface VerificationTask {
        boolean passed();
        void markPassed();
    }
    public static class BaseVerificationTask implements VerificationTask {
        private volatile boolean passed = false;
        @Override
        public boolean passed() {
            return passed;
        }

        @Override
        public void markPassed() {
            passed = true;
        }
    }
    private static List<VerificationTask> tasks;
    VerificationTask task;
    public Verification(VerificationTask task) {
        this.task = task;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection tasks() {
        PetraProgram.parseSrcFiles();
        tasks = new ArrayList<>();
        for (CompilationUnitWithData cu : PetraProgram.all.values().stream().filter(c->
                c.clazz.isInterface() &&
                !Consumer.class.isAssignableFrom(c.clazz) &&
                !c.clazz.isAnnotationPresent(Primative.class)).collect(Collectors.toList())) {
            tasks.add(new ProveViewSoundnessAndCompletenessTask(cu));
        }
        PetraProgram.LOG.debug("views: "+PetraProgram.dataTypeInfoMap);

        for (CompilationUnitWithData cu : PetraProgram.all.values().stream().filter(c-> Consumer.class.isAssignableFrom(c.clazz) && !PEdge.class.isAssignableFrom(c.clazz)).collect(Collectors.toList())) {
            String term = cu.compilationUnit.toString();
            term = term.replaceAll("empty empty","i");
            CompilationUnit programTerm = new JavaParser().parse(term).getResult().get();
            cu.compilationUnit = programTerm;
            int count = 0;
            for (Expression kase : cu.compilationUnit
                    .getClassByName(cu.clazz.getSimpleName()).get()
                    .getMethodsByName("accept").get(0).getBody().get()
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
                tasks.add(new ProveKaseTask(count,kase,cu));
                count++;
            }
        }
        return tasks;
    }

    static class ProveViewSoundnessAndCompletenessTask extends BaseVerificationTask {
        public ProveViewSoundnessAndCompletenessTask(CompilationUnitWithData view) {
            this.view = view;
        }
        private CompilationUnitWithData view;

        @Override
        public String toString() {
            return "View:"+view.clazz.getSimpleName();
        }
    }

    static class ProveKaseTask extends BaseVerificationTask {
        int count;
        Expression kase;
        CompilationUnitWithData cu;
        public ProveKaseTask(int count, Expression kase, CompilationUnitWithData cu) {
            this.count = count;
            this.kase = kase;
            this.cu = cu;
        }

        @Override
        public String toString() {
            return "Kase"+count+":"+cu.clazz.getSimpleName();
        }
    }

    @Test
    public void test() throws ClassNotFoundException {
        if (task instanceof ProveKaseTask){
            ProveKaseTask proveKaseTask = (ProveKaseTask) task;
            PetraProgram.rewriteSingleJoinPar(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            PetraProgram.rewriteGraphKaseStepsToEdges(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            PetraProgram.rewriteGraphKaseJoinParStepsToEdges(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            PetraProgram.rewriteStepsWithForall(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            PetraProgram.rewriteGraphKaseSeperatedStepsToNonSeperatedSteps(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            PetraProgram.rewriteJoinForallParSteps(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            PetraProgram.rewriteGraphKaseJoinParsToSeq(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            PetraProgram.rewriteSingleParStepToSeqStep(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            if (PetraProgram.rewriteKase(
                    proveKaseTask.cu.clazz.isAnnotationPresent(Infinite.class) &&
                            proveKaseTask.cu.clazz.getSimpleName().equals(PetraProgram.rootGraphName) && proveKaseTask.cu.compilationUnit
                            .getClassByName(proveKaseTask.cu.clazz.getSimpleName()).get()
                            .getMethodsByName("accept").get(0).getBody().get()
                            .asBlockStmt()
                            .getStatements()
                            .get(0)
                            .asExpressionStmt()
                            .getExpression()
                            .asMethodCallExpr()
                            .getArguments().size()-1==proveKaseTask.count,proveKaseTask.kase, proveKaseTask.cu,proveKaseTask.count)){
                task.markPassed();
                assertTrue(true);
            } else {
                fail();
            }
        } else if(task instanceof ProveViewSoundnessAndCompletenessTask){
            ProveViewSoundnessAndCompletenessTask soundnessAndCompletenessTask = (ProveViewSoundnessAndCompletenessTask)task;
            ClassOrInterfaceDeclaration c = soundnessAndCompletenessTask.view.compilationUnit
                    .getInterfaceByName(soundnessAndCompletenessTask.view.clazz.getSimpleName()).get();
            boolean result =  PetraProgram.isViewSoundAndComplete(soundnessAndCompletenessTask.view.compilationUnit,soundnessAndCompletenessTask.view.clazz,c);
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
            PetraProgram.convertToControlledEnglish();
        }
    }

    protected static void setRoot(Class<? extends Consumer> root) {
        if (PEdge.class.isAssignableFrom(root)){
            throw new UnsupportedOperationException();
        }
        PetraProgram.rootGraphName = root.getSimpleName();
        PetraProgram.entryPointPackageName = root.getPackage().getName();
    }
}
