package com.cognitionbox.petra.examples.tradingsystem4.decision.views;


import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.lang.primitives.impls.PInteger;

// having separate default interfaces means each can be analysed in isolation,
// ie allows for separation to reduce state space and therefore reduces complexity of abstract soundness/completeness
// checking, relative to the information provided in the interface only.
@Primative
public interface CountsView {
    PInteger winnersCount();
    PInteger losersCount();
    PInteger waitingCount();
    PInteger failedCount();
    PInteger successCount();
    PInteger badQuoteCount();
    
    default boolean winnersCountIncrementedBy1() {
        return winnersCount().isIncrementedBy(1);
    }
    default boolean losersCountIncrementedBy1() {
        return losersCount().isIncrementedBy(1);
    }
    default boolean waitingCountIncrementedBy1() {
        return waitingCount().isIncrementedBy(1);
    }
    default boolean failedSubmissionCountIncrementedBy1() {
        return failedCount().isIncrementedBy(1);
    }
    default boolean successSubmissionCountIncrementedBy1() {
        return successCount().isIncrementedBy(1);
    }
    default boolean badQuoteCountIncrementedBy1() {
        return badQuoteCount().isIncrementedBy(1);
    }
}
