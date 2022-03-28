package com.cognitionbox.petra.verification.tasks;

public interface VerificationTask {
        boolean passed();
        void markPassed();
    }