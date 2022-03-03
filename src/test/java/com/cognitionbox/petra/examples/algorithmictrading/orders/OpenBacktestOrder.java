package com.cognitionbox.petra.examples.algorithmictrading.orders;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.examples.algorithmictrading.system.TradingSystem;

import java.util.function.Consumer;

@Edge
public class OpenBacktestOrder implements Consumer<TradingSystem> {
    @Override
    public void accept(TradingSystem ts) {

    }
}
