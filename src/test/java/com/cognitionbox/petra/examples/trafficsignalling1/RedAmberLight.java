package com.cognitionbox.petra.examples.trafficsignalling1;

public interface RedAmberLight {
    RedAmberLightState state();
    AmberLightPeriodPassed periodPassed();
}
