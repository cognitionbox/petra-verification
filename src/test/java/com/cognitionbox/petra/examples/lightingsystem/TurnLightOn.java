package com.cognitionbox.petra.examples.lightingsystem;


import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

/*
 * seq(l.s(),new SwitchOn());
 * seq(l.p(),new PowerOn());
 */

public class TurnLightOn implements Consumer<LightView> {
    @Override
    public void accept(LightView light) {
        kases(light,
                kase(l->l.off(), l->l.on(),l->{
                    join(par(l.s(),new SwitchOn()), par(l.p(),new PowerOn()));
                })
            );
    }
}
