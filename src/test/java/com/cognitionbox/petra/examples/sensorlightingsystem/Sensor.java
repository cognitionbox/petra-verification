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
    PInteger lumens();

    default boolean greaterThanOrEqualTo10000Lumens(){
        return lumens().gt(new PInteger(10000));
    }

    default boolean lessThanOrEqualTo10000Lumens(){
        return lumens().lt(new PInteger(10000));
    }

    @Edge static void updateSensorReading(Sensor s){
        kases(s,kase(
                sensor->sensor.greaterThanOrEqualTo10000Lumens() ^ sensor.lessThanOrEqualTo10000Lumens(),
                sensor->sensor.greaterThanOrEqualTo10000Lumens() ^ sensor.lessThanOrEqualTo10000Lumens(),
                sensor->sensor.lumens().set((int) Math.sin(Math.PI/LocalTime.now().getSecond())))
        );
    }
}
