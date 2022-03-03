package com.cognitionbox.petra.examples.algorithmictrading.orders;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.examples.algorithmictrading.system.TradingSystem;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class SubmitOrder implements Consumer<TradingSystem> {
    @Override
    public void accept(TradingSystem ts) {
        kases(ts,
            kase(
                tradingSystem->tradingSystem.decision().isOpen().get()!=null,
                tradingSystem->true,
                tradingSystem->{
                    if (tradingSystem.decision().open()){
                        ts.csvDecisionWriter().writeOpenOrder(tradingSystem.decision());
                    } else if (!tradingSystem.decision().isOpen().get()){
                        ts.csvDecisionWriter().writeCloseOrder(tradingSystem.decision());
                    }
                    tradingSystem.decision().hold();
                }
            )
        );
    }
}
