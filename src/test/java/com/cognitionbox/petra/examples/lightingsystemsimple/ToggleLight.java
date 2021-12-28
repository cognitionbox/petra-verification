package com.cognitionbox.petra.examples.lightingsystemsimple;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class ToggleLight implements Consumer<Light> {
    @Override
    public void accept(Light light) {
        kases(light,
                kase(l->l.off(), l->l.on(),new TurnLightOn()),
                kase(l->l.on(), l->l.off(),new TurnLightOff())
        );
    }
}
