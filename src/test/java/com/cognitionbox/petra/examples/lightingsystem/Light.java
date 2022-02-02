package com.cognitionbox.petra.examples.lightingsystem;

public interface Light {
    Button button();
    Power power();
    default boolean on(){
        return button().on() && power().on();
    }

    // below is same as (button().off() || power().off()), it just demonstrates expressibility
    default boolean off(){
        return (button().on() ^ button().off()) & (power().on() ^ power().off()) & !(button().on() & power().on());
    }

    default void turnOn(){
        power().powerOn();
        button().switchOn();
    }

    default void turnOff(){
        power().powerOff();
        button().switchOff();
    }
}
