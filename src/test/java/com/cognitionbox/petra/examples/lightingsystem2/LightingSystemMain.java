package com.cognitionbox.petra.examples.lightingsystem2;

import static com.cognitionbox.petra.lang.Petra.start;

public class LightingSystemMain {
    public static void main(String... args){
        start(Light::toggleLight,Light.class);
    }
}
