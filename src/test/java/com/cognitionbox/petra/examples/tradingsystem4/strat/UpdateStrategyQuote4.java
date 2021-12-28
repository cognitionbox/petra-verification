package com.cognitionbox.petra.examples.tradingsystem4.strat;


import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.examples.tradingsystem4.decision.Strategy;
import com.cognitionbox.petra.examples.tradingsystem4.marketdata.igindex.IGindexHelper;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.invkase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class UpdateStrategyQuote4 implements Consumer<Strategy> {

    @Override
    public void accept(Strategy s) {
        kases(s,
                invkase(
                        strategy->strategy.parameters(), parameters->parameters.isPrepared(),
                        strategy->strategy.strategyState(), strategyState->strategyState.goodQuote() ^ strategyState.badQuote(),
                        (parameters,strategyState)->{
                            strategyState.bid().set(IGindexHelper.getBid(parameters.instrument().get().epic).get());
                            strategyState.ask().set(IGindexHelper.getAsk(parameters.instrument().get().epic).get());
//                            parameters.limit().set(parameters.direction().get()== Direction.BUY?quoteView.ask().get():quoteView.bid().get());
//                            parameters.exit().set(parameters.direction().get()== Direction.BUY?quoteView.ask().get()+1:quoteView.bid().get()-1);
//                            parameters.stop().set(parameters.direction().get()== Direction.BUY?quoteView.ask().get()-5:quoteView.bid().get()+5);
                            strategyState.quoteTimeStamp().set(System.currentTimeMillis());
                }));
    }

}
