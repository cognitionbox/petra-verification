package com.cognitionbox.petra.examples.sensorlightingsystem;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.lang.primitives.impls.PBoolean;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@View
@Primative
public interface Power {
    PBoolean active();
    default boolean off(){
        return !active().get();
    }
    default boolean on() {
        return active().get();
    }

    @Edge static void powerOff(Power b){
        kases(b,kase(power->true, power->power.off(), button->{button.active().set(false);}));
    }

    @Edge
    static void powerOn(Power b){
        kases(b,kase(power->true, power->power.on(), button->{button.active().set(true);}));
    }


}
