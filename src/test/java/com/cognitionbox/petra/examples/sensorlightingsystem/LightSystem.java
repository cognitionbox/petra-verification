package com.cognitionbox.petra.examples.sensorlightingsystem;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.annotations.View;

import static com.cognitionbox.petra.lang.Petra.*;

@View public interface LightSystem {
    Threshold threshold();
    Light light();

    default boolean isDarkAndLightOff(){return threshold().isDark() && light().off();}
    default boolean isDarkAndLightOn(){return threshold().isDark() && light().on();}
    default boolean isLightAndLightOff(){return threshold().isLight() && light().off();}
    default boolean isLightAndLightOn(){return threshold().isLight() && light().on();}

    static void updateLight(LightSystem l){
        kases(l,
                kase(lightSystem->lightSystem.isDarkAndLightOff(), lightSystem->lightSystem.isDarkAndLightOn(), lightSystem->{
                    join(lightSystem,
                            par(ls->ls.threshold(), Threshold::skipIfDark),
                            par(ls->ls.light(), Light::lightOn));
                    seq(lightSystem, LightSystem::message1);
                }),
                kase(lightSystem->lightSystem.isDarkAndLightOn(), lightSystem->lightSystem.isDarkAndLightOn(), lightSystem->{
                    seq(lightSystem, LightSystem::message2);
                }),
                kase(lightSystem->lightSystem.isLightAndLightOn(), lightSystem->lightSystem.isLightAndLightOff(), lightSystem->{
                    seq(lightSystem.threshold(), Threshold::skipIfLight);
                    seq(lightSystem.light(), Light::lightOff);
                    seq(lightSystem, LightSystem::message3);
                }),
                kase(lightSystem->lightSystem.isLightAndLightOff(), lightSystem->lightSystem.isLightAndLightOff(), lightSystem->{
                    seq(lightSystem, LightSystem::message4);
                })
        );
    }

    @Edge static void message1(LightSystem l){
        kases(l,kase(lightSystem->lightSystem.isDarkAndLightOn(), lightSystem->lightSystem.isDarkAndLightOn(), lightSystem->{
            System.out.println(lightSystem.threshold().sensor().lumens()+" Lumens. Is dark and light off, hence TURN ON.");
        }));
    }

    @Edge static void message2(LightSystem l){
        kases(l,kase(lightSystem->lightSystem.isDarkAndLightOn(), lightSystem->lightSystem.isDarkAndLightOn(), lightSystem->{
            System.out.println(lightSystem.threshold().sensor().lumens()+" Lumens. Is dark and light already on, hence no change.");
        }));
    }

    @Edge static void message3(LightSystem l){
        kases(l,kase(lightSystem->lightSystem.isLightAndLightOff(), lightSystem->lightSystem.isLightAndLightOff(), lightSystem->{
            System.out.println(lightSystem.threshold().sensor().lumens()+" Lumens. Is light and light on, hence TURN OFF.");
        }));
    }

    @Edge static void message4(LightSystem l){
        kases(l,kase(lightSystem->lightSystem.isLightAndLightOff(), lightSystem->lightSystem.isLightAndLightOff(), lightSystem->{
            System.out.println(lightSystem.threshold().sensor().lumens()+" Lumens. Is light and light already off, hence no change.");
        }));
    }
}
