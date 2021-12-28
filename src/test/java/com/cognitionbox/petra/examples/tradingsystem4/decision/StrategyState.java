package com.cognitionbox.petra.examples.tradingsystem4.decision;

import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.Direction;
import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.PStatus;
import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.PSubmission;
import com.cognitionbox.petra.examples.tradingsystem4.decision.views.*;
import com.cognitionbox.petra.lang.primitives.PValue;
import com.cognitionbox.petra.lang.primitives.impls.PDouble;
import com.cognitionbox.petra.lang.primitives.impls.PLong;

public class StrategyState implements
        QuoteView,
        QuotesTimeStampView,
        StrategyDecisionStateView,
        BidVsLimitView,
        AskVsLimitView,
        BidVsStopView,
        AskVsStopView,
        BidVsExitView,
        AskVsExitView,
        DirectionView,
        StatusView,
        SubmissionView {
    private PLong quoteTimeStamp = new PLong();
    private PDouble bid = new PDouble();
    private PDouble ask = new PDouble();
    private PLong quoteTimestamp = new PLong();
    private PStatus status = new PStatus();
    private PDouble limit = new PDouble();
    private PDouble stop = new PDouble();
    private PDouble exit = new PDouble();
    private PValue<Direction> direction = new PValue<>();
    private PSubmission submission = new PSubmission();

    public PDouble limit() {
        return limit;
    }

    public PDouble stop() {
        return stop;
    }

    public PDouble exit() {
        return exit;
    }

    public PValue<Direction> direction() {
        return direction;
    }

    @Override
    public PLong quoteTimeStamp() {
        return quoteTimeStamp;
    }

    @Override
    public StrategyState strategyState() {
        return this;
    }

    public PDouble bid() {
        return bid;
    }

    public PDouble ask() {
        return ask;
    }

    @Override
    public DirectionView directionView() {
        return null;
    }


    @Override
    public QuoteView quoteView() {
        return this;
    }

    @Override
    public BidVsLimitView bidVsLimitView() {
        return this;
    }

    @Override
    public AskVsLimitView askVsLimitView() {
        return this;
    }

    @Override
    public BidVsExitView bidVsExitView() {
        return this;
    }

    @Override
    public AskVsExitView askVsExitView() {
        return this;
    }

    @Override
    public BidVsStopView bidVsStopView() {
        return this;
    }

    @Override
    public AskVsStopView askVsStopView() {
        return this;
    }

//    @Override
//    public boolean isStopped() {
//        return StrategyDecisionStateView.super.isStopped();
//    }
//
//    @Override
//    public boolean isExited() {
//        return StrategyDecisionStateView.super.isExited();
//    }

    public StatusView statusView() {
        return this;
    }

    public PStatus status() {
        return status;
    }

    @Override
    public PSubmission submission() {
        return submission;
    }
}
