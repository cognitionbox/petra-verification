package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.lang.step.PGraph;
import static com.cognitionbox.petra.lang.Petra.*;

/*
 * seq(l.s(),new SwitchOn());
 * seq(l.p(),new PowerOn());
 */

public interface ToggleLight extends PGraph<Light>{
    static void accept(Light l) {
        kases(l,
                kase(light->light.off(), light->light.on(),light->{
                    join(light,
                            par(light_->light_.button(),SwitchOn::accept),
                            par(light_->light_.power(),PowerOn::accept));
                }),
                kase(light->light.on(), light->light.off(),light->{
                    seq(light.button(),SwitchOff::accept);
                    seq(light.power(),PowerOff::accept);
                })
            );
    }
}
