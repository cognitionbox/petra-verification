package com.cognitionbox.petra.examples.tradingsystem4.marketdata.views;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.Instrument;

import java.util.List;

@Primative
public interface StartedStatesView {
    List<Boolean> started();
    default boolean isStarted(Instrument i) {return started().get(i.ordinal());}
    default boolean notStarted(Instrument i) {return !started().get(i.ordinal());}
    default void start(Instrument i){
        started().set(i.ordinal(),true);
    }
}
