package com.cognitionbox.petra.examples.trafficsignalling1;

public interface AmberLight {
    AmberLightState state();
    AmberLightPeriodPassed periodPassed();
}
