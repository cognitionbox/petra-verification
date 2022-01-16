package com.cognitionbox.petra.examples.lightingsystem;

import static com.cognitionbox.petra.lang.Petra.finiteStart;

public class LightingSystemMain {
    public static void main(String... args){
        finiteStart(new TurnLightOn(),new Light());
    }
}
