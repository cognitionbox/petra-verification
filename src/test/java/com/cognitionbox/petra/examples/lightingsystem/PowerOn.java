package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class PowerOn implements Consumer<PowerView> {
    @Override
    public void accept(PowerView power) {
        kases(power,
                kase(p->p.off() ^ p.on(), p->p.on(),p->{
                    p.powerOn();
                    //System.out.println(Thread.currentThread());
                })
        );
    }
}
