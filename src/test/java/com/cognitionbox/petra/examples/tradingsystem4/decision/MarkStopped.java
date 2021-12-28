package com.cognitionbox.petra.examples.tradingsystem4.decision;


import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.Status;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class MarkStopped implements Consumer<Strategy> {

    @Override
    public void accept(Strategy s) {
        kases(s, kase(strategy->strategy.strategyState().isOpenBuyAndShouldStop(), strategy->strategy.strategyState().isStopped(), strategy->{
            strategy.strategyState().status().set(Status.STOPPED);
        }));
    }
}
