package com.cognitionbox.petra.verification.tasks;

import com.cognitionbox.petra.verification.CompilationUnitWithData;
import com.github.javaparser.ast.expr.Expression;

import java.util.List;
import java.util.Set;

public class ProveKaseTask extends BaseVerificationTask {
        private final String methodName;
        private final int count;
        private final Expression kase;
        private final CompilationUnitWithData cu;

    private final Set<List<String>> overlappingStates;
        public ProveKaseTask(String methodName, int count, Expression kase, CompilationUnitWithData cu, Set<List<String>> overlappingStates) {
            this.methodName = methodName;
            this.count = count;
            this.kase = kase;
            this.cu = cu;
            this.overlappingStates = overlappingStates;
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
            return overlappingStates.isEmpty();
        }

    public Set<List<String>> getOverlappingStates() {
        return overlappingStates;
    }
    }