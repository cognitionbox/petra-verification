package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.lang.step.PEdge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;


public class LightOff implements PEdge<Light> {
    @Override
    public void accept(Light l) {
        kases(l,
                kase(light->light.on(), light->light.off(),light->{
                    light.turnOff();
                })
        );
    }
}
