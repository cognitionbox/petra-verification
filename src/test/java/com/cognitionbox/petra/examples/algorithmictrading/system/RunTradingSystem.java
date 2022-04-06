package com.cognitionbox.petra.examples.algorithmictrading.system;

import com.cognitionbox.petra.lang.step.PGraph;
import com.cognitionbox.petra.examples.algorithmictrading.marketdata.InitDecisionWriterIfNeeded;
import com.cognitionbox.petra.examples.algorithmictrading.marketdata.LoadCsvMarketDataIfNeeded;
import com.cognitionbox.petra.examples.algorithmictrading.marketdata.UpdateDecisionQuote;
import com.cognitionbox.petra.examples.algorithmictrading.orders.SubmitOrder;
import com.cognitionbox.petra.examples.algorithmictrading.strategy.PerfectOrderStrategy;


import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

public interface RunTradingSystem extends PGraph<TradingSystem> {
    static  void accept(TradingSystem t) {
        kases(t,
                kase(tradingSystem->true,
                        tradingSystem->true,
                        tradingSystem->{
                            seq(tradingSystem.csvDecisionWriter(), InitDecisionWriterIfNeeded::accept);
                            seq(tradingSystem.csvMarketData(), LoadCsvMarketDataIfNeeded::accept);
                            seq((ModeWithDecisionsWithQuotes) tradingSystem, UpdateDecisionQuote::accept);
                            seq(tradingSystem, PerfectOrderStrategy::accept);
                            seq(tradingSystem, SubmitOrder::accept);
                            //seq(tradingSystem.csvDecisionWriter(), new CloseCsvDecisionWriter());
                        })
        );
    }
}
