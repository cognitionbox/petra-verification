package com.cognitionbox.petra.modelling4;

import static com.cognitionbox.petra.modelling4.Modelling.*;

public class TrafficSignallingSystemModelMain {
    public static void main(String[] args){
        SYS s = system("SIGNALLING");
        STATE_SPACE ss = s.statespace();
        TRANSITIONS t = s.transitions();
        STATE trafficLights = ss.xor("RED","RED_AMBER","GREEN","AMBER");
        STATE periodPassed = ss.xor("PASSED","NOT_PASSED");
        STATE walkRequested = ss.xor("WALK_REQUESTED","WALK_NOT_REQUESTED");
        STATE pedestrianLights = ss.xor("WALK","DONT_WALK");
        STATE trafficLightsAndPeriodPassed = trafficLights.with(periodPassed);
        STATE trafficLightsAndWalkRequested = trafficLights.with(walkRequested);
        STATE trafficLights2 = trafficLightsAndPeriodPassed.without(periodPassed);
        ss.add(trafficLights,
            periodPassed,
            walkRequested,
            pedestrianLights,
            pedestrianLights,
            trafficLightsAndPeriodPassed,
            trafficLightsAndWalkRequested,
            trafficLights2);
        TRAN tt = tran(ss.state("RED"),ss.state("AMBER"));
        TRAN t1 = tran(ss.state("RED"),ss.state("RED_AMBER"));
        TRAN t2 = tran(ss.state("RED_AMBER"),ss.state("GREEN"));
        TRAN t3 = tran(ss.state("GREEN"),ss.state("AMBER"));
        TRANS ts = trans(t1,t2,t3);
        TABS abs = new TABS(ts,tt);
        t.add(t1);
        t.add(ts);
        t.add(abs);
        ss.render();
        t.render();
        System.out.println(ss.verify());
        System.out.println(t.verify());
    }
}
