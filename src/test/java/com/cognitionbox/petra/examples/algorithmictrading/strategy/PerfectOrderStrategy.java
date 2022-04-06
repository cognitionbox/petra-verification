package com.cognitionbox.petra.examples.algorithmictrading.strategy;

import com.cognitionbox.petra.lang.step.PEdge;
import com.cognitionbox.petra.examples.algorithmictrading.marketdata.Quote;
import com.cognitionbox.petra.examples.algorithmictrading.marketdata.QuoteImpl;
import com.cognitionbox.petra.examples.algorithmictrading.system.TradingSystem;
import com.cognitionbox.petra.lang.primitives.impls.PBigDecimal;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;
import static com.cognitionbox.petra.lang.primitives.PBigDecimals.TWO;
import static java.math.BigDecimal.ONE;


public interface PerfectOrderStrategy extends PEdge<TradingSystem> {
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
                        PBigDecimal mid = bid.add(ask).divide(TWO);

                        Quote q = new QuoteImpl();
                        q.bid().set(bid);
                        q.ask().set(ask);
                        strategy.twenty().add(q);
                        strategy.fifty().add(q);
                        strategy.oneHundred().add(q);
                        strategy.twoHundred().add(q);

                        if (
                            mid.gt(strategy.twenty().calc()) &&
                            strategy.twenty().calc().gt(strategy.fifty().calc()) &&
                            strategy.fifty().calc().gt(strategy.oneHundred().calc()) &&
                            strategy.oneHundred().calc().gt(strategy.twoHundred().calc())
                        ){
                            // Buy
                            if (strategy.decision().notOpen()){
                                strategy.decision().buy();
                            } else {
                                strategy.decision().close();
                            }
                        } else if (
                                mid.lt(strategy.twenty().calc()) &&
                                        strategy.twenty().calc().lt(strategy.fifty().calc()) &&
                                        strategy.fifty().calc().lt(strategy.oneHundred().calc()) &&
                                        strategy.oneHundred().calc().lt(strategy.twoHundred().calc())
                        ){
                            // Sell
                            if (strategy.decision().notOpen()){
                                strategy.decision().sell();
                            } else {
                                strategy.decision().close();
                            }
                        }
                    }
                )
        );
    }
}
