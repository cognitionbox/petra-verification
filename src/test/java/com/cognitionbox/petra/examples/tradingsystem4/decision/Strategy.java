package com.cognitionbox.petra.examples.tradingsystem4.decision;

import com.cognitionbox.petra.examples.tradingsystem4.Counts;
import com.cognitionbox.petra.examples.tradingsystem4.decision.views.*;
import com.cognitionbox.petra.lang.primitives.impls.PDouble;

public class Strategy implements StrategyStatusAndPreparedView
{ // just implement one interface here, so composition happens in the interfaces
    private Counts counts;
    private FinalParameters parameters = new FinalParameters();
    private StrategyState strategyState = new StrategyState();

    private PDouble bid = new PDouble();
    private PDouble ask = new PDouble();
    private PDouble lastPnl = new PDouble();
    private PDouble rrr = new PDouble();
    private PDouble stopDistance = new PDouble();
    private PDouble entryPrice = new PDouble();
    private PDouble closePrice = new PDouble();

    public FinalParameters parameters(){
        return parameters;
    }

    public StrategyState strategyState(){
        return strategyState;
    }

    public Counts countsView() {
        return counts;
    }

    @Override
    public StrategyPreparedView preparedView() {
        return parameters;
    }

    @Override
    public StatusView statusView() {
        return strategyState;
    }
}
