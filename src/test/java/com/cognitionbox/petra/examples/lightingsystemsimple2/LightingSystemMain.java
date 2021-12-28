package com.cognitionbox.petra.examples.lightingsystemsimple2;

import static com.cognitionbox.petra.lang.Petra.finiteStart;

public class LightingSystemMain {
    public static void main(String... args){
        finiteStart(new TurnLightOn(),new Light());
    }
}
