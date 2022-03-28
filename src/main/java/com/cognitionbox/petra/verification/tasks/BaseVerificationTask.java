package com.cognitionbox.petra.verification.tasks;

public class BaseVerificationTask implements VerificationTask {
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