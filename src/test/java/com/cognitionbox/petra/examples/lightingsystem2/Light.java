package com.cognitionbox.petra.examples.lightingsystem2;

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
                    seq(light.button(),Button::buttonOff);
                    seq(light.power(),Power::powerOff);
                    //seq(light,Light::print);
                }));
    }

    @Graph static void lightOn(Light l){
        kases(l,
                kase(light->light.off(), light->light.on(),light->{
                    join(light,
                            par(lht->lht.button(),Button::buttonOn),
                            par(lht->lht.power(),Power::powerOn));
                    //seq(light,Light::print);
                }));
    }

    @Edge static void print(Light l){
        kases(l,
                kase(light->light.on() ^ light.off(), light->light.on() ^ light.off(),light->{
                    System.out.println(light.on()?"ON":"OFF");
                }));
    }

    @Graph static void toggleLight(Light l){
        kases(l,
                kase(light->light.off(), light->light.on(),light->{
                    seq(light,Light::lightOn);
                }),
                kase(light->light.on(), light->light.off(),light->{
                    seq(light,Light::lightOff);
                })
        );
    }
}
