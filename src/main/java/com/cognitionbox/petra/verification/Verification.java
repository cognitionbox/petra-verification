 /*
  * CONFIDENTIAL - COGNITION BOX LTD.
  *
  * Copyright 2016-2022 Cognition Box Ltd. - All Rights Reserved
  *
  * This file is part of "petra-verification-0.1.0-alpha.jar".
  * "petra-verification-0.1.0-alpha.jar" is owned by:
  *
  * Cognition Box Ltd. (10194162)
  * 9 Grovelands Road,
  * Palmers Green,
  * London, N13 4RJ
  * England.
  *
  * All information contained herein is, and remains the property of
  * Cognition Box Ltd. The intellectual and technical concepts contained
  * herein are proprietary to Cognition Box Ltd. and may be covered
  * by patents in process, and are protected by trade secret or
  * copyright law. Dissemination of this information or reproduction
  * of this material is strictly forbidden unless prior written
  * permission is obtained from Cognition Box Ltd.
  *
  * "petra-verification-0.1.0-alpha.jar" includes trade secrets of Cognition Box Ltd.
  * In order to protect "petra-verification-0.1.0-alpha.jar", You shall not decompile, reverse engineer, decrypt,
  * extract or disassemble "petra-verification-0.1.0-alpha.jar" or otherwise reduce or attempt to reduce any software
  * in "petra-verification-0.1.0-alpha.jar" to source code form. You shall ensure, both during and
  * (if you still have possession of "petra-verification-0.1.0-alpha.jar") after the performance of this Agreement,
  * that (i) persons who are not bound by a confidentiality agreement consistent with this Agreement
  * shall not have access to "petra-verification-0.1.0-alpha.jar" and (ii) persons who are so bound are put on written notice that
  * "petra-verification-0.1.0-alpha.jar" contains trade secrets, owned by and proprietary to Cognition Box Ltd.
  *
  * "petra-verification-0.1.0-alpha.jar" is Proprietary and Confidential.
  * Unauthorized copying of "petra-verification-0.1.0-alpha.jar" files, via any medium is strictly prohibited.
  *
  * "petra-verification-0.1.0-alpha.jar" can not be copied and/or distributed without the express
  * permission of Cognition Box Ltd.
  *
  * "petra-verification-0.1.0-alpha.jar" is written by Aran Hakki.
  */

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
    @Parameterized.Parameters(name = "{0}")//{index}: stepTest[{0}]={1}")
    public static Collection tasks() {
        PetraProgram.parseSrcFiles();

//        try {
//            if (!PetraProgram.checkRootKases(PetraProgram.all.get(Class.forName(PetraProgram.entryPointPackageName +"."+ PetraProgram.rootGraphName)))){
//                throw new IllegalStateException("root kases are invalid");
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

        //PetraProgram.verifyViews2(); // enable after updating trading system
        tasks = new ArrayList<>();
        for (CompilationUnitWithData cu : PetraProgram.all.values().stream().filter(c->
                //c.clazz.isInterface() &&
                c.clazz.isInterface() &&
                        //!c.clazz.getSimpleName().startsWith("P") &&
                        !Consumer.class.isAssignableFrom(c.clazz) &&
                        !c.clazz.isAnnotationPresent(Primative.class)).collect(Collectors.toList())) {
            tasks.add(new ProveViewSoundnessAndCompletenessTask(cu));
        }
        PetraProgram.LOG.debug("views: "+PetraProgram.dataTypeInfoMap);

        //tasks.addAll(PetraProgram.all.values().stream().map(c->new ParseFileTask(c)).collect(Collectors.toList()));
        for (CompilationUnitWithData cu : PetraProgram.all.values().stream().filter(c-> Consumer.class.isAssignableFrom(c.clazz) && !PEdge.class.isAssignableFrom(c.clazz)).collect(Collectors.toList())) {
            String term2 = cu.compilationUnit.toString();
            term2 = term2.replaceAll("empty empty","i");
            CompilationUnit programTerm2 = new JavaParser().parse(term2).getResult().get();
            cu.compilationUnit = programTerm2;
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

    static class ParseFileTask extends BaseVerificationTask {
        CompilationUnitWithData cu;
        public ParseFileTask(CompilationUnitWithData cu) {
            this.cu = cu;
        }

        @Override
        public String toString() {
            return "Parsing of "+cu.path.toString().split("\\\\")[cu.path.toString().split("\\\\").length-1];
        }
    }

    @Test
    public void test() throws ClassNotFoundException {
        if (task instanceof ProveKaseTask){
            ProveKaseTask proveKaseTask = (ProveKaseTask) task;
//            if (!PetraProgram.checkRootKasesForCompletenessAndSoundnessAgainstAView(((ProveKaseTask) task).cu)){
//                throw new IllegalStateException("root kases are invalid, not sound and complete wrt to a view.");
//            }
            PetraProgram.rewriteSingleJoinPar(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            //rewriteCasts();
            PetraProgram.rewriteGraphKaseStepsToEdges(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            PetraProgram.rewriteGraphKaseJoinParStepsToEdges(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            PetraProgram.rewriteStepsWithForall(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            PetraProgram.rewriteGraphKaseSeperatedStepsToNonSeperatedSteps(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            PetraProgram.rewriteJoinForallParSteps(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            PetraProgram.rewriteGraphKaseJoinParsToSeq(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            //PetraProgram.rewriteGraphKaseJoinParsOrSeperatedSeqsToSeq(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            PetraProgram.rewriteSingleParStepToSeqStep(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
            //PetraProgram.checkKasePreAndPostConditionsFallInsideADistinctView(proveKaseTask.kase,proveKaseTask.cu,proveKaseTask.count);
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
        } else if (task instanceof ParseFileTask){
//            ParseFileTask parseFileTask = (ParseFileTask) task;
//            System.out.println(parseFileTask.cu.path.toString());
//            ParseNodeAndRuleMatchResults res = BullwinkleCli.doMain(new String[]{"C:\\Users\\aranh\\Downloads\\petra-java.bnf",parseFileTask.cu.path.toString()}, System.in, System.out, System.err);
//            if (res.parseNode==null){
//                fail();
//            } else {
//                task.markPassed();
//            }
        }
    }

    @AfterClass
    public static void after(){
//        if (PetraProgram.all.values().stream()
//                .filter(c->Consumer.class.isAssignableFrom(c.clazz) && !c.clazz.isAnnotationPresent(Edge.class))
//                .allMatch(cu ->!cu.status.values().isEmpty() && cu.status.values().stream().allMatch(proved->proved==true))){
//            PetraProgram.convertToControlledEnglish();
//        }

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
