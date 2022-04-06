package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.lang.step.PEdge;
import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;


public interface SwitchOff extends PEdge<Button>{
    static void accept(Button b) {
        kases(b,
                kase(button->button.on(), button->button.off(),button->{
                    button.active().set(false);
                })
        );
    }
}
