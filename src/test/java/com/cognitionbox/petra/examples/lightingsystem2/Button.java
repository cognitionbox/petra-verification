package com.cognitionbox.petra.examples.lightingsystem2;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.lang.primitives.impls.PBoolean;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Primative
@View
public interface Button {
    PBoolean active();





    @Edge static void buttonOff(Button b){
        kases(b,
                kase(button->true, button->button.off(), button->{
                    button.active().set(false);
                })
        );
    }

    @Edge
    static void buttonOn(Button b){
        kases(b,
                kase(button->true, button->button.on(), button->{
                    button.active().set(true);
                })
        );
    }

    default boolean off(){
        return !active().get();
    }
    default boolean on() {
        return active().get();
    }
}
