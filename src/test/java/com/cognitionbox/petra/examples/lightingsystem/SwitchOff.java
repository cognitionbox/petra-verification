package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class SwitchOff implements Consumer<Button> {
    @Override
    public void accept(Button b) {
        kases(b,
                kase(button->button.on(), button->button.off(),button->{
                    button.switchOff();
                })
        );
    }
}
