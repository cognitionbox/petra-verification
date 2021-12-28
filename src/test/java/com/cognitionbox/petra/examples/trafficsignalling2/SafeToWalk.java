package com.cognitionbox.petra.examples.trafficsignalling2;

public interface SafeToWalk {
    TrafficLightState trafficLight();
    default boolean isSafe(){
        return trafficLight().isRed();
    }
    default boolean isNotSafe(){
        return !trafficLight().isRed();
    }

}
