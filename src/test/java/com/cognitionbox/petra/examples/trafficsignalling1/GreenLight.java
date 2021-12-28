package com.cognitionbox.petra.examples.trafficsignalling1;

public interface GreenLight {
    GreenLightState state();
    GreenLightPeriodPassed periodPassed();
}
