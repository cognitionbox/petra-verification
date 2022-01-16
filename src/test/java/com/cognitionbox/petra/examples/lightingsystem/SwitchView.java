package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.lang.primitives.impls.PBoolean;

@Primative
public interface SwitchView {
    PBoolean active();
    default void switchOn(){
        active().set(true);
    }
    default void switchOff(){
        active().set(false);
    }
    default boolean off(){
        return !active().get();
    }

    default boolean on() {
        return active().get();
    }
}
