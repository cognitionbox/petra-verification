package com.cognitionbox.petra.examples.tradingsystem4.strat;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.examples.tradingsystem4.TradingSystem;
import com.cognitionbox.petra.examples.tradingsystem4.decision.Strategy;
import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.Direction;
import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.Instrument;
import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.Status;
import com.cognitionbox.petra.examples.tradingsystem4.marketdata.igindex.IGindexHelper;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class PopulateStrategies implements Consumer<TradingSystem> {

    @Override
    public void accept(TradingSystem ts) {
        // cannot mix and match props from different interface views within a condition, but can across conditions
        // this ensures only disjoint observations are made
        kases(ts, kase(
                tradingSystem->tradingSystem.strategiesEmpty(),
                tradingSystem->tradingSystem.strategiesPrepared(),
                tradingSystem->{
                    IGindexHelper.updateMarketSnapshots(
                            IGindexHelper.getCst(),
                            IGindexHelper.getX_security_token(),
                            tradingSystem.strategies().stream().map(s->s.parameters().instrument().get().epic).collect(Collectors.toList()));
                    Strategy s1 = new Strategy();
                    s1.parameters().instrument().set(Instrument.SPTRD);
                    s1.strategyState().direction().set(Direction.BUY);
                    s1.strategyState().status().set(Status.WAITING);
                    s1.strategyState().bid().set(IGindexHelper.getBid(s1.parameters().instrument().get().epic).get());
                    s1.strategyState().ask().set(IGindexHelper.getAsk(s1.parameters().instrument().get().epic).get());
                    s1.strategyState().limit().set(s1.strategyState().direction().get()== Direction.BUY?s1.strategyState().ask().get():s1.strategyState().bid().get());
                    s1.strategyState().exit().set(s1.strategyState().direction().get()== Direction.BUY?s1.strategyState().ask().get()+1:s1.strategyState().bid().get()-1);
                    s1.strategyState().stop().set(s1.strategyState().direction().get()== Direction.BUY?s1.strategyState().ask().get()-5:s1.strategyState().bid().get()+5);
                    tradingSystem.add(s1);
        }));
    }
}
