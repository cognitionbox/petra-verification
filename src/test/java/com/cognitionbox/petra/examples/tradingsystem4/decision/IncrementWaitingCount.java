package com.cognitionbox.petra.examples.tradingsystem4.decision;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class IncrementWaitingCount implements Consumer<Strategy> {
    @Override
    public void accept(Strategy s) {
        kases(s, kase(strategy->strategy.strategyState().isWaitingAndShouldNotBuy() ^ strategy.strategyState().isWaitingAndShouldNotSell(),
                strategy->strategy.strategyState().isWaitingAndShouldNotBuy() ^ strategy.strategyState().isWaitingAndShouldNotSell() ^
                        strategy.strategyState().isOpenBuyAndShouldWait() ^ strategy.strategyState().isOpenBuyAndShouldStop() ^ strategy.strategyState().isOpenBuyAndShouldExit() ^
                        strategy.strategyState().isOpenSellAndShouldWait() ^ strategy.strategyState().isOpenSellAndShouldStop() ^ strategy.strategyState().isOpenSellAndShouldExit(),
            strategy-> {
                strategy.countsView().winnersCount().isIncrementedBy(1);
            }));
    }
}
