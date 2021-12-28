package com.cognitionbox.petra.examples.tradingsystem4;

import com.cognitionbox.petra.examples.tradingsystem4.decision.views.CountsView;
import com.cognitionbox.petra.lang.primitives.impls.PInteger;

public class Counts implements CountsView {
    public PInteger badQuoteCount = new PInteger();
    public PInteger waitingCount = new PInteger();
    // submissions
    public PInteger successCount = new PInteger();
    public PInteger failedCount = new PInteger();
    // outcomes
    public PInteger winnersCount = new PInteger();
    public PInteger losersCount = new PInteger();

    @Override
    public PInteger winnersCount() {
        return winnersCount;
    }

    @Override
    public PInteger losersCount() {
        return losersCount;
    }

    @Override
    public PInteger waitingCount() {
        return waitingCount;
    }

    @Override
    public PInteger failedCount() {
        return failedCount;
    }

    @Override
    public PInteger successCount() {
        return successCount;
    }

    @Override
    public PInteger badQuoteCount() {
        return badQuoteCount;
    }
}
