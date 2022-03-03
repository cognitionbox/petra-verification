package com.cognitionbox.petra.examples.algorithmictrading.system;

import com.cognitionbox.petra.lang.primitives.impls.PBoolean;

public interface Mode {
    PBoolean mode();
    default boolean isHistoricalMode(){return !isLiveMode();}
    default boolean isLiveMode(){return mode().get();}
}
