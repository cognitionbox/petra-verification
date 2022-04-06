package com.cognitionbox.petra.examples.algorithmictrading.orders;

import com.cognitionbox.petra.lang.step.PEdge;
import com.cognitionbox.petra.examples.algorithmictrading.system.TradingSystem;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;


public interface SubmitOrder extends PEdge<TradingSystem> {
    static  void accept(TradingSystem ts) {
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
