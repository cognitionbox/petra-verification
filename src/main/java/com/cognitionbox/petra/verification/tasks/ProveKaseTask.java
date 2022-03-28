package com.cognitionbox.petra.verification.tasks;

import com.cognitionbox.petra.verification.CompilationUnitWithData;
import com.github.javaparser.ast.expr.Expression;

public class ProveKaseTask extends BaseVerificationTask {
        private final int count;
        private final Expression kase;
        private final CompilationUnitWithData cu;
        public ProveKaseTask(int count, Expression kase, CompilationUnitWithData cu) {
            this.count = count;
            this.kase = kase;
            this.cu = cu;
        }

        @Override
        public String toString() {
            return "Kase"+count+":"+cu.getClazz().getSimpleName();
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