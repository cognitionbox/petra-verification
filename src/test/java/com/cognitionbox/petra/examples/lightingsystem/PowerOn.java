package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.lang.step.PEdge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;


public class PowerOn implements PEdge<Power> {
    @Override
    public void accept(Power p) {
        kases(p,
                kase(power->power.off() ^ power.on(), power->power.on(),power->{
                    power.powerOn();
                    //System.out.println(Thread.currentThread());
                })
        );
    }
}
