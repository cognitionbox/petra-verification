package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class SwitchOff implements Consumer<Switch> {
    @Override
    public void accept(Switch swt) {
        kases(swt,
                kase(s->s.on(), s->s.off(),s->{
                    s.switchOff();
                })
        );
    }
}
