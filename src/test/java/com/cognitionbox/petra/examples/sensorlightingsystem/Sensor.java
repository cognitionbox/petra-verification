package com.cognitionbox.petra.examples.sensorlightingsystem;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.lang.primitives.impls.PInteger;

import java.time.LocalTime;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Primative
@View
public interface Sensor {
    // assumed
    @Edge default PInteger lumens(){return new PInteger((int) ((Math.sin( LocalTime.now().getSecond() * ((2*Math.PI)/60))*10000)+10000));}

    default boolean greaterThanOrEqualTo10000Lumens(){
        return lumens().gt(new PInteger(10000));
    }

    default boolean lessThanOrEqualTo10000Lumens(){
        return lumens().lt(new PInteger(10000));
    }
}
