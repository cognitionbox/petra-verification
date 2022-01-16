package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.lang.primitives.impls.PBoolean;

@Primative
public interface PowerView {
    PBoolean active();
    default void powerOn(){
        active().set(true);
    }
    default void powerOff(){
        active().set(false);
    }
    default boolean off(){
        return !active().get();
    }

    default boolean on() {
        return active().get();
    }
}
