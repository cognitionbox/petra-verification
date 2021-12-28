package com.cognitionbox.petra.examples.lightingsystem;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

public class ToggleLightSystem implements Consumer<LightSystem> {
    @Override
    public void accept(LightSystem lightSystem) {
        kases(lightSystem,
            kase(   l->forall(l.lights,i->i.off()),
                    l->forall(l.lights,i->i.on()),
                    l->{
                        seqr(l.lights,new ToggleLight());
                    }),
            kase(   l->forall(l.lights,i->i.on()),
                    l->forall(l.lights,i->i.off()),
                    l->{
                        seqr(l.lights,new ToggleLight());
                    })
            );
    }
}
