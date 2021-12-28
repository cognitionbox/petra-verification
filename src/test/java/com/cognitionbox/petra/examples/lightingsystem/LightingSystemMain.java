package com.cognitionbox.petra.examples.lightingsystem;

import static com.cognitionbox.petra.lang.Petra.infiniteStart;

public class LightingSystemMain {
    public static void main(String... args){
        infiniteStart(new ToggleLightSystem(),new LightSystem(), 1000);
    }
}
