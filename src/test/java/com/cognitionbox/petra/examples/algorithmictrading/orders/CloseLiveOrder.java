package com.cognitionbox.petra.examples.algorithmictrading.orders;

import com.cognitionbox.petra.lang.step.PEdge;
import com.cognitionbox.petra.examples.algorithmictrading.system.TradingSystem;

import java.util.function.Consumer;


public interface CloseLiveOrder extends PEdge<TradingSystem> {
    static  void accept(TradingSystem ts) {

    }
}
