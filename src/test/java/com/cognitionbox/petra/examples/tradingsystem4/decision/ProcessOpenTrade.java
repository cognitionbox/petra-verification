package com.cognitionbox.petra.examples.tradingsystem4.decision;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.Status;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class ProcessOpenTrade implements Consumer<StrategyState> {
    @Override
    public void accept(StrategyState s) {
        kases(s,
                kase(strategyState->strategyState.isOpenSellAndShouldStop(),
                        strategyState->strategyState.isStopped(),
                        strategyState->{
                            strategyState.status().set(Status.STOPPED);
                }),
                kase(strategyState->strategyState.isOpenBuyAndShouldStop(),
                        strategyState->strategyState.isStopped(),
                        strategyState->{
                            strategyState.status().set(Status.STOPPED);
                }),
            kase(strategyState->strategyState.isOpenBuyAndShouldExit() ^ strategyState.isOpenSellAndShouldWait(),
                    strategyState->strategyState.isExited(),
                    strategyState->{
                strategyState.status().set(Status.EXITED);
            }));
    }
}
