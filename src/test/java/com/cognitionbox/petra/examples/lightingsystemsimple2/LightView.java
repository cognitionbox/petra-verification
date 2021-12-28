package com.cognitionbox.petra.examples.lightingsystemsimple2;
// s().off() || p().off()
public interface LightView {
    SwitchView s();
    PowerView p();
    default boolean on(){
        return s().on() && p().on();
    }

    default boolean off(){
        return (s().on() ^ s().off()) & (p().on() ^ p().off()) & !(s().on() & p().on());
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
