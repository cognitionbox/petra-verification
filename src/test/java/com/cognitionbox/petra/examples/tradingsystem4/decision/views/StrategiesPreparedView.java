package com.cognitionbox.petra.examples.tradingsystem4.decision.views;


import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.examples.tradingsystem4.decision.Strategy;

// having separate default interfaces means each can be analysed in isolation,
// ie allows for separation to reduce state space and therefore reduces complexity of abstract soundness/completeness
// checking, relative to the information provided in the interface only.
@Primative
public interface StrategiesPreparedView extends StrategiesView{
    default boolean strategiesEmpty() {
        return strategies().isEmpty();
    }
    default boolean strategiesPrepared() {return strategies().forall(strategy->strategy.parameters().isPrepared());}
    default void add(Strategy s1){
        strategies().add(s1);
    }
}
