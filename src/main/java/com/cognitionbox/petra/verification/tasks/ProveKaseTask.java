package com.cognitionbox.petra.verification.tasks;

import com.cognitionbox.petra.verification.CompilationUnitWithData;
import com.github.javaparser.ast.expr.Expression;

public class ProveKaseTask extends BaseVerificationTask {
        private final String methodName;
        private final int count;
        private final Expression kase;
        private final CompilationUnitWithData cu;

    private final boolean kasesAreXOR;
        public ProveKaseTask(String methodName, int count, Expression kase, CompilationUnitWithData cu, boolean kasesAreXOR) {
            this.methodName = methodName;
            this.count = count;
            this.kase = kase;
            this.cu = cu;
            this.kasesAreXOR = kasesAreXOR;
        }

        @Override
        public String toString() {
            return methodName+":"+cu.getClazz().getSimpleName()+":kase"+count;
        }

        public int getCount() {
            return count;
        }

        public Expression getKase() {
            return kase;
        }

        public CompilationUnitWithData getCompilationUnitWithData() {
            return cu;
        }

        public boolean isKasesAreXOR() {
            return kasesAreXOR;
        }
    }