package com.cognitionbox.petra.examples.trafficsignalling2;

public interface TrafficLightAndWalkRequest {
    // 16 states
    TrafficLight trafficLight();
    WalkRequested walkRequested();

    default boolean doChangeFromRedToRedAmber(){
        return trafficLight().redLightPeriodPassed();
    }

    default boolean doChangeFromRedAmberToGreen(){
        return trafficLight().redAmberLightPeriodPassed();
    }

    default boolean doChangeFromGreenToAmber(){
        return trafficLight().greenLightPeriodPassed() && walkRequested().isTrue();
    }

    default boolean doChangeFromAmberToRed(){
        return trafficLight().amberLightPeriodPassed();
    }

    default boolean dontChange(){
        return (trafficLight().redLightPeriodNotPassed() ||
                trafficLight().redAmberLightPeriodNotPassed() ||
                trafficLight().greenLightPeriodNotPassed() ||
                trafficLight().amberLightPeriodNotPassed()) ^
                trafficLight().greenLightPeriodPassed() && walkRequested().isFalse()
        ;
    }

}
