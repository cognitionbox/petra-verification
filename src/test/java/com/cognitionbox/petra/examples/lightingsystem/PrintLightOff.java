package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class PrintLightOff implements Consumer<Light> {
    @Override
    public void accept(Light l) {
        kases(l,
                kase(light->light.off(), light->light.off(),light->{
                    System.out.println("Light switched Off.");
                })
        );
    }
}
