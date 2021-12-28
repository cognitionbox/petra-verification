package com.cognitionbox.petra.examples.lightingsystem;

public interface LightView {
    Switch s();
    Power p();
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
