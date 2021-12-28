package com.cognitionbox.petra.examples.trafficsignalling2;

public interface TrafficLight {

    TrafficLightState state();
    PeriodPassed periodPassed();

    default boolean redLightPeriodPassed(){
        return state().isRed() && periodPassed().isTrue();
    }

    default boolean redAmberLightPeriodNotPassed(){
        return state().isRedAmber() && periodPassed().isFalse();
    }

    default boolean redAmberLightPeriodPassed(){
        return state().isRedAmber() && periodPassed().isTrue();
    }

    default boolean greenLightPeriodNotPassed(){
        return state().isGreen() && periodPassed().isFalse();
    }

    default boolean greenLightPeriodPassed(){
        return state().isGreen() && periodPassed().isTrue();
    }

    default boolean amberLightPeriodNotPassed(){
        return state().isAmber() && periodPassed().isFalse();
    }

    default boolean amberLightPeriodPassed(){
        return state().isAmber() && periodPassed().isTrue();
    }

    default boolean redLightPeriodNotPassed(){
        return state().isRed() && periodPassed().isFalse();
    }

}
