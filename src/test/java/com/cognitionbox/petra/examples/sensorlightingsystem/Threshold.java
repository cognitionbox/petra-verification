package com.cognitionbox.petra.examples.sensorlightingsystem;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.annotations.View;

import static com.cognitionbox.petra.lang.Petra.*;

@View
public interface Threshold {
    Sensor sensor();

    default boolean isLight(){
        return sensor().greaterThanOrEqualTo10000Lumens();
    }
    default boolean isDark(){
        return sensor().lessThanOrEqualTo10000Lumens();
    }

    @Edge static void skipIfLight(Threshold t){
        kases(t,kase(threshold->threshold.isLight(), threshold->threshold.isLight(), time->{}));
    }

    @Edge static void skipIfDark(Threshold t){
        kases(t,kase(threshold->threshold.isDark(), threshold->threshold.isDark(), time->{}));
    }
}
