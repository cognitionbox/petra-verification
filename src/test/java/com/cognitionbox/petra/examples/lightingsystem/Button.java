package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.lang.primitives.impls.PBoolean;

@Primative @View public interface Button {
    PBoolean active();

    default boolean off(){
        return !active().get();
    }
    default boolean on() {
        return active().get();
    }
}
