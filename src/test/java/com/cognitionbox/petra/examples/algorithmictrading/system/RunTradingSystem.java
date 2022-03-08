package com.cognitionbox.petra.examples.algorithmictrading.system;

import com.cognitionbox.petra.lang.step.PGraph;
import com.cognitionbox.petra.examples.algorithmictrading.marketdata.InitDecisionWriterIfNeeded;
import com.cognitionbox.petra.examples.algorithmictrading.marketdata.LoadCsvMarketDataIfNeeded;
import com.cognitionbox.petra.examples.algorithmictrading.marketdata.UpdateDecisionQuote;
import com.cognitionbox.petra.examples.algorithmictrading.orders.SubmitOrder;
import com.cognitionbox.petra.examples.algorithmictrading.strategy.PerfectOrderStrategy;


import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

public class RunTradingSystem implements PGraph<TradingSystem> {
    @Override
    public void accept(TradingSystem t) {
        kases(t,
                kase(tradingSystem->true,
                        tradingSystem->true,
                        tradingSystem->{
                            seq(tradingSystem.csvDecisionWriter(), new InitDecisionWriterIfNeeded());
                            seq(tradingSystem.csvMarketData(), new LoadCsvMarketDataIfNeeded());
                            seq((ModeWithDecisionsWithQuotes) tradingSystem, new UpdateDecisionQuote());
                            seq(tradingSystem, new PerfectOrderStrategy());
                            seq(tradingSystem, new SubmitOrder());
                            //seq(tradingSystem.csvDecisionWriter(), new CloseCsvDecisionWriter());
                        })
        );
    }
}
