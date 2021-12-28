package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class SwitchOn implements Consumer<Switch> {
    @Override
    public void accept(Switch swt) {
        kases(swt,
                kase(s->s.off(), s->s.on(),s->{
                    s.switchOn();
                    //System.out.println(Thread.currentThread());
                })
        );
    }
}
