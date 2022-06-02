package com.cognitionbox.petra.examples.sensorlightingsystem;

import static com.cognitionbox.petra.lang.Petra.infiniteStart;

public class LightingSystemMain {
    public static void main(String... args){
        infiniteStart(LightSystem::updateLight, LightSystem.class,1000);

        // we to check the time interval, and make sure the period of the check aligns with the interval,
        // so we dont miss any states.
        // like trigger if time is 00:00, and ensure checks are performed every minute.
        // only trigger when changes from day to night
        //infiniteStart(LightSystem::updateLight, LightSystem.class, 1000);
    }
}
