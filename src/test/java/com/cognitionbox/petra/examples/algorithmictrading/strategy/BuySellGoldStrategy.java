package com.cognitionbox.petra.examples.algorithmictrading.strategy;

import com.cognitionbox.petra.lang.step.PEdge;
import com.cognitionbox.petra.examples.algorithmictrading.system.TradingSystem;
import com.cognitionbox.petra.lang.primitives.impls.PBigDecimal;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;
import static java.math.BigDecimal.ONE;


public interface BuySellGoldStrategy extends PEdge<TradingSystem> {
    static  void accept(TradingSystem t) {
        kases(t,
                kase(
                    strategy->true,
                    strategy->true,
                    strategy->{
                        strategy.decision().symbol().set("XAUUSD");
                        strategy.decision().qty().set(ONE);
                        PBigDecimal bid = strategy.decision().bid();
                        PBigDecimal ask = strategy.decision().ask();
                        if (
                                bid.eq(new PBigDecimal("1959.29")) &&
                                ask.eq(new PBigDecimal("1959.3"))
                        ){
                            // Buy
                            strategy.decision().sell();
                        } else if (
                                bid.eq(new PBigDecimal("1900.43")) &&
                                ask.eq(new PBigDecimal("1900.44"))
                        ){
                            // Sell
                            strategy.decision().close();
                        } else {
                            strategy.decision().hold();
                        }
                    }
                )
        );
    }
}
