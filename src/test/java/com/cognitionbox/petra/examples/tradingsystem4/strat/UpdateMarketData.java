package com.cognitionbox.petra.examples.tradingsystem4.strat;


import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.examples.tradingsystem4.TradingSystem;
import com.cognitionbox.petra.examples.tradingsystem4.marketdata.igindex.IGindexHelper;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class UpdateMarketData implements Consumer<TradingSystem> {

    @Override
    public void accept(TradingSystem ts) {
        kases(ts,
                kase(
                        tradingSystem->tradingSystem.strategiesEmpty(),
                        tradingSystem->tradingSystem.strategiesEmpty(),
                        tradingSystem -> IGindexHelper.updateMarketSnapshots(
                                IGindexHelper.getCst(),
                                IGindexHelper.getX_security_token(),
                                tradingSystem.strategies().stream().map(s->s.parameters().instrument().get().epic).collect(Collectors.toList()))));
    }

}
