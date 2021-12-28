package com.cognitionbox.petra.modelling5;

import static com.cognitionbox.petra.modelling5.MODELLING.*;

public class TrafficSignallingSystemModelMain {
    public static void main(String[] args){
        SYSTEM system = system("SIGNALLING");

        STATE red = state("RED");
        STATE red_amber = state("RED_AMBER");
        STATE green = state("GREEN");
        STATE amber = state("AMBER");

        STATE passed = state("PASSED");
        STATE not_passed = state("NOT_PASSED");

        STATE traffic_lights_state = xor(red,red_amber,green,amber);
        STATE periodPassed = xor(passed,not_passed);

        STATE walk_requested = state("WALK_REQUESTED");
        STATE walk_not_requested = state("WALK_NOT_REQUESTED");

        STATE walk_requested_state = xor(walk_requested,walk_not_requested);

        STATE walk = state("WALK");
        STATE dont_walk = state("DONT_WALK");

        STATE pedestrian_lights_state = xor(walk,dont_walk);
        STATE traffic_lights_and_period_passed = traffic_lights_state.with(periodPassed);
        STATE traffic_lights_and_walk_requested = traffic_lights_state.with(walk_requested_state);

        STATE seperated = traffic_lights_and_period_passed.without(periodPassed);
        system.add(traffic_lights_state,
            periodPassed,
            walk_requested_state,
            pedestrian_lights_state,
            pedestrian_lights_state,
            traffic_lights_and_period_passed,
            traffic_lights_and_walk_requested,
            seperated);
        TRAN tt = tran(red,amber);
        TRAN t1 = tran(red,red_amber);
        TRAN t2 = tran(red_amber,green);
        TRAN t3 = tran(green,amber);
        TRANS ts = trans(t1,t2,t3);
        TABS abs = ts.abs(tt);
        system.add(t1);
        system.add(ts);
        system.add(abs);
        system.renderStates();
        system.renderTransitions();
        System.out.println(system.verifyStates());
        System.out.println(system.verifyTransitions());
    }
}
