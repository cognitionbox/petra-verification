package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.lang.step.PEdge;
import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;


public interface SwitchOn extends PEdge<Button> {
    static void accept(Button b) {
        kases(b,
                kase(button->button.off() ^ button.on(), button->button.on(),button->{
                    button.active().set(true);
                })
        );
    }
}
