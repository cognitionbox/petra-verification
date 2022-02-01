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
                kase(light_->light_.off(), light_->light_.on(),light_->{
                    join(light_,par(light__->light__.s(),new SwitchOn()), par(light__->light__.p(),new PowerOn()));
                })
            );
    }
}
