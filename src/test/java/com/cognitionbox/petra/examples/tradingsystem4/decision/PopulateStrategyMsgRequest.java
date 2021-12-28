package com.cognitionbox.petra.examples.tradingsystem4.decision;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class PopulateStrategyMsgRequest implements Consumer<Strategy> {

    @Override
    public void accept(Strategy strategy) {
        kases(strategy,
            kase(s->s.strategyState().isWaitingAndShouldBuy(), s->s.strategyState().isStopped(), s->{

            }),
            kase(s->s.strategyState().isWaitingAndShouldSell(), s->s.strategyState().isStopped(), s->{

            })
        );
    }
}
