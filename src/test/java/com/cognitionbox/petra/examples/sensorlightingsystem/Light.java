package com.cognitionbox.petra.examples.sensorlightingsystem;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.annotations.Graph;
import com.cognitionbox.petra.annotations.View;

import static com.cognitionbox.petra.lang.Petra.*;

@View public interface Light {
    Button button();
    Power power();

    default boolean off(){ return button().off() || power().off();}
    default boolean on(){return button().on() && power().on();}

    @Graph static void lightOff(Light l){
        kases(l,
                kase(light->light.on(), light->light.off(),light->{
                    join(light,
                            par(lht->lht.button(), Button::buttonOff),
                            par(lht->lht.power(), Power::powerOff));
                }));
    }

    @Graph static void lightOn(Light l){
        kases(l,
                kase(light->light.off(), light->light.on(),light->{
                    join(light,
                            par(lht->lht.button(), Button::buttonOn),
                            par(lht->lht.power(), Power::powerOn));
                }));
    }

    @Edge static void skip(Light l){
        kases(l,kase(light->true,light->true,light->{}));
    }
}
