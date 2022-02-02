package com.cognitionbox.petra.examples.lightingsystem;


import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

/*
 * seq(l.s(),new SwitchOn());
 * seq(l.p(),new PowerOn());
 */

public class ToggleLight implements Consumer<Light> {
    @Override
    public void accept(Light l) {
        kases(l,
                kase(light->light.off(), light->light.on(),light->{
                    join(light,
                            par(light_->light_.button(),new SwitchOn()),
                            par(light_->light_.power(),new PowerOn()));
                    seq(light, new PrintLightOn());
                }),
                kase(light->light.on(), light->light.off(),light->{
                    seq(light.button(),new SwitchOff());
                    seq(light.power(),new PowerOff());
                    seq(light, new PrintLightOff());
                })
            );
    }
}
