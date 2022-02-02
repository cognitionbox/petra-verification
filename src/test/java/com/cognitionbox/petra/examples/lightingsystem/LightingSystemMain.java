package com.cognitionbox.petra.examples.lightingsystem;

import static com.cognitionbox.petra.lang.Petra.start;

public class LightingSystemMain {
    public static void main(String... args){
        start(new ToggleLight(),new LightImpl());
    }
}
