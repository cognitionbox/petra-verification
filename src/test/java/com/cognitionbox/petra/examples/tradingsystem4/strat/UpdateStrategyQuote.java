package com.cognitionbox.petra.examples.tradingsystem4.strat;


import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.examples.tradingsystem4.decision.Strategy;
import com.cognitionbox.petra.examples.tradingsystem4.marketdata.igindex.IGindexHelper;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class UpdateStrategyQuote implements Consumer<Strategy> {

    @Override
    public void accept(Strategy s) {
        kases(s,
                kase(
                        strategy->strategy.parameters().isPrepared(),
                        strategy->strategy.parameters().isPrepared() && (strategy.strategyState().goodQuote() ^ strategy.strategyState().badQuote()),
                        strategy->{
                            strategy.strategyState().bid().set(IGindexHelper.getBid(strategy.parameters().instrument().get().epic).get());
                            strategy.strategyState().ask().set(IGindexHelper.getAsk(strategy.parameters().instrument().get().epic).get());
//                            parameters.limit().set(parameters.direction().get()== Direction.BUY?quoteView.ask().get():quoteView.bid().get());
//                            parameters.exit().set(parameters.direction().get()== Direction.BUY?quoteView.ask().get()+1:quoteView.bid().get()-1);
//                            parameters.stop().set(parameters.direction().get()== Direction.BUY?quoteView.ask().get()-5:quoteView.bid().get()+5);
                            strategy.strategyState().quoteTimeStamp().set(System.currentTimeMillis());
                }));
    }

}
