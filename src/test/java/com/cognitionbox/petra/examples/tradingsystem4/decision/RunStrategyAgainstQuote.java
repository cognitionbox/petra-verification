package com.cognitionbox.petra.examples.tradingsystem4.decision;

import com.cognitionbox.petra.examples.tradingsystem4.decision.submit.MarkDoNotSubmit;
import com.cognitionbox.petra.examples.tradingsystem4.decision.submit.MarkDoSubmit;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;
// cannot mix and match props from different interface views within a condition, but can across conditions
// this ensures only disjoint observations are made

// cannot mix and match props from different interface views within a condition, but can across conditions
// this ensures only disjoint observations are made
public class RunStrategyAgainstQuote implements Consumer<StrategyState> {
    @Override
    public void accept(StrategyState s) {

        kases(s,
                kase(strategyState->
                                strategyState.isBadQuote() ^
                                strategyState.isWaitingAndShouldNotBuy() ^
                                strategyState.isWaitingAndShouldNotSell(),
                        strategyState -> strategyState.doNotSubmit(),
                        strategyState->{
                    seq(strategyState, new MarkDoNotSubmit());
                }),
                kase(
                        strategyState->
                                strategyState.isWaitingAndShouldBuy() ^
                                strategyState.isWaitingAndShouldSell(),
                        strategyState->strategyState.doSubmit(),
                        strategyState->{
                        seq(strategyState, new MarkDoSubmit());
                    })
                ,
                kase(strategyState->
                                strategyState.isOpenBuyAndShouldStop() ^
                                strategyState.isOpenSellAndShouldStop() ^
                                strategyState.isOpenBuyAndShouldExit() ^
                                strategyState.isOpenSellAndShouldExit(),
                        strategyState -> strategyState.isStopped() ^ strategyState.isExited() ,
                        strategyState->{
                            seq(strategyState, new ProcessOpenTrade());
                        })
        );
    }
}
