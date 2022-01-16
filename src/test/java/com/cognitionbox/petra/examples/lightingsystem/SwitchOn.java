package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class SwitchOn implements Consumer<SwitchView> {
    @Override
    public void accept(SwitchView swt) {
        kases(swt,
                kase(s->s.off() ^ s.on(), s->s.on(),s->{
                    s.switchOn();
                    //System.out.println(Thread.currentThread());
                })
        );
    }
}
