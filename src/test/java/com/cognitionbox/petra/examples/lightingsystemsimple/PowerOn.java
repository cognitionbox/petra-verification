package com.cognitionbox.petra.examples.lightingsystemsimple;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class PowerOn implements Consumer<Power> {
    @Override
    public void accept(Power power) {
        kases(power,
                kase(p->p.off(), p->p.on(),p->{
                    p.powerOn();
                    //System.out.println(Thread.currentThread());
                })
        );
    }
}
