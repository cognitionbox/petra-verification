package com.cognitionbox.petra.examples.trafficsignalling1;

public interface TrafficLight {
    RedLight redLight();
    RedAmberLight redAmberLight();
    GreenLight greenLight();
    AmberLight amberLight();

    default boolean redLightPeriodPassed(){
        return redLight().state().isRed() && redLight().periodPassed().isTrue();
    }

    default boolean redAmberLightPeriodPassed(){
        return redAmberLight().state().isRedAmber() && redLight().periodPassed().isTrue();
    }

    default boolean greenLightPeriodPassed(){
        return greenLight().state().isGreen() && greenLight().periodPassed().isTrue();
    }

    default boolean amberLightPeriodPassed(){
        return amberLight().state().isAmber() && amberLight().periodPassed().isTrue();
    }

    default boolean periodNotPassed(){
        return redLight().periodPassed().isFalse() ||
                redAmberLight().periodPassed().isFalse() ||
                greenLight().periodPassed().isFalse() ||
                amberLight().periodPassed().isFalse();
    }

}
