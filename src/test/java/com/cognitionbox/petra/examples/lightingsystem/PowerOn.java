package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.lang.step.PEdge;
import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;


public interface PowerOn  extends PEdge<Power>{
    static void accept(Power p) {
        kases(p,
                kase(power->power.off() ^ power.on(), power->power.on(), power->{
                    power.active().set(true);
                    //System.out.println(Thread.currentThread());
                })
        );
    }
}
