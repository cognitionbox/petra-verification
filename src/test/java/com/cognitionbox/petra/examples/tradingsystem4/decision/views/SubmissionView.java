package com.cognitionbox.petra.examples.tradingsystem4.decision.views;


import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.PSubmission;
import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.Submission;

// having separate default interfaces means each can be analysed in isolation,
// ie allows for separation to reduce state space and therefore reduces complexity of abstract soundness/completeness
// checking, relative to the information provided in the interface only.
@Primative
public interface SubmissionView {
    PSubmission submission();
    default boolean doSubmit() {return submission().get()== Submission.DO_SUBMIT;}
    default boolean submitted() {return submission().get()== Submission.SUBMITTED;}
    default boolean failedToSubmit() {return submission().get()== Submission.FAILED_TO_SUBMIT;}
    default boolean doNotSubmit() {return submission().get()== Submission.DO_NOT_SUBIT;}
}
