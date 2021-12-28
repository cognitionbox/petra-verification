package com.cognitionbox.petra.examples.tradingsystem4;

import com.cognitionbox.petra.examples.tradingsystem4.decision.Strategy;
import com.cognitionbox.petra.examples.tradingsystem4.decision.views.*;
import com.cognitionbox.petra.examples.tradingsystem4.marketdata.Quotes;
import com.cognitionbox.petra.lang.collection.PCollection;
import com.cognitionbox.petra.lang.collection.PList;


public class TradingSystem implements StrategiesEffectedView, StrategiesPreparedView, TradeTimeView {
    //private final StrategiesManager strategiesManager = new StrategiesManager();

    private final PCollection<Strategy> strategies = new PList<>();
    public PCollection<Strategy> strategies() {
        return strategies;
    }

    private Quotes quotes;
    public Quotes quotes() {
        return quotes;
    }

//    public boolean strategiesPreparedAndEffected() {
//        return strategies().forall(strategy->strategy.parameters().isPrepared()) && strategiesEffectedView().effected());
//    }
}
