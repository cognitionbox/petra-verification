package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.annotations.View;

@View public interface Light {
    Button button();
    Power power();
    default boolean on(){
        return button().on() && power().on();
    }

    // below is same as (button().off() || power().off()), it just demonstrates expressibility
    default boolean off(){
        return (button().on() ^ button().off()) & (power().on() ^ power().off()) & !(button().on() & power().on());
    }
}
