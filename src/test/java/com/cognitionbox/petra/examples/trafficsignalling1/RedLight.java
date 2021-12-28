package com.cognitionbox.petra.examples.trafficsignalling1;

public interface RedLight {
    RedLightState state();
    RedLightPeriodPassed periodPassed();
}
