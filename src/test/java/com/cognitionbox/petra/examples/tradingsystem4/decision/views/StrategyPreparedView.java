package com.cognitionbox.petra.examples.tradingsystem4.decision.views;

import com.cognitionbox.petra.annotations.Primative;

// having separate default interfaces means each can be analysed in isolation,
// ie allows for separation to reduce state space and therefore reduces complexity of abstract soundness/completeness
// checking, relative to the information provided in the interface only.
@Primative
public interface StrategyPreparedView extends InstrumentView {
    default boolean isPrepared(){
        return hasInstument();
    }
    default boolean notPrepared(){
        return !isPrepared();
    }
}
