package com.cognitionbox.petra.examples.lightingsystemsimple;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

public class ToggleLightSystem implements Consumer<LightSystem> {
    @Override
    public void accept(LightSystem lightSystem) {
        kases(lightSystem,
            kase(   l->l.light.off(),
                    l->l.light.on(),
                    l->{
                        seq(l.light,new TurnLightOn());
                    }),
                kase(   l->l.light.on(),
                        l->l.light.off(),
                        l->{
                            seq(l.light,new TurnLightOff());
                        })
            );
    }
}
