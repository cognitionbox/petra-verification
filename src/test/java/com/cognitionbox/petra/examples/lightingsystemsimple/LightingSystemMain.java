package com.cognitionbox.petra.examples.lightingsystemsimple;

import static com.cognitionbox.petra.lang.Petra.finiteStart;

public class LightingSystemMain {
    public static void main(String... args){
        finiteStart(new ToggleLightSystem(),new LightSystem());
    }
}
