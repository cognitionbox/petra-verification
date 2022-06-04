package com.cognitionbox.petra.examples.sensorlightingsystem;

import static com.cognitionbox.petra.lang.Petra.infiniteStart;

public class LightingSystemMain {
    public static void main(String... args){
        infiniteStart(LightSystem::updateLight, LightSystem.class,1000);
    }
}
