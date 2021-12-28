package com.cognitionbox.petra.modelling3;

import static com.cognitionbox.petra.modelling3.Modelling.*;

public class TrafficSignallingSystemModelMain {
    public static void main(String[] args){
        SYS s = system("SIGNALLING");
        STATE trafficLights = xor("RED","RED_AMBER","GREEN","AMBER");
        STATE periodPassed = xor("PASSED","NOT_PASSED");
        STATE walkRequested = xor("WALK_REQUESTED","WALK_NOT_REQUESTED");
        STATE pedestrianLights = xor("WALK","DONT_WALK");
        STATE trafficLightsAndPeriodPassed = trafficLights.with(periodPassed);
        STATE trafficLightsAndWalkRequested = trafficLights.with(walkRequested);
        STATE trafficLights2 = trafficLightsAndPeriodPassed.without(periodPassed);
        s.add(trafficLights,
            periodPassed,
            walkRequested,
            pedestrianLights,
            pedestrianLights,
            trafficLightsAndPeriodPassed,
            trafficLightsAndWalkRequested,
            trafficLights2);
        s.render();
    }
}
