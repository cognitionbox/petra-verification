package com.cognitionbox.petra.verification.tasks;

import com.cognitionbox.petra.verification.CompilationUnitWithData;
import com.github.javaparser.ast.expr.Expression;

public class ProveKaseTask extends BaseVerificationTask {
        private final String methodName;
        private final int count;
        private final Expression kase;
        private final CompilationUnitWithData cu;
        public ProveKaseTask(String methodName, int count, Expression kase, CompilationUnitWithData cu) {
            this.methodName = methodName;
            this.count = count;
            this.kase = kase;
            this.cu = cu;
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
    }