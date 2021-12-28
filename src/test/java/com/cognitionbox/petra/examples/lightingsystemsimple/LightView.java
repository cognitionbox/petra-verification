package com.cognitionbox.petra.examples.lightingsystemsimple;

public interface LightView {
    SwitchView s();
    PowerView p();
    default boolean on(){
        return s().on() && p().on();
    }

    default boolean off(){
        return s().off() || p().off();
    }

    default void turnOn(){
        p().powerOn();
        s().switchOn();
    }

    default void turnOff(){
        p().powerOff();
        s().switchOff();
    }
}
