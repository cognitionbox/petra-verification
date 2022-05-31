package com.cognitionbox.petra.examples.sensorlightingsystem;

import com.cognitionbox.petra.annotations.View;

import static com.cognitionbox.petra.lang.Petra.*;

@View public interface LightSystem {
    Threshold threshold();
    Light light();

    default boolean isDarkAndLightOff(){return threshold().isDark() && light().off();}
    default boolean isDarkAndLightOn(){return threshold().isDark() && light().on();}
    default boolean isLightAndLightOff(){return threshold().isLight() && light().off();}
    default boolean isLightAndLightOn(){return threshold().isLight() && light().on();}

    static void updateSystem(LightSystem l){
        kases(l,
                kase(
                        lightSystem->lightSystem.isDarkAndLightOff() ^ lightSystem.isDarkAndLightOn() ^ lightSystem.isLightAndLightOff() ^ lightSystem.isLightAndLightOn(),
                        lightSystem->lightSystem.isDarkAndLightOff() ^ lightSystem.isDarkAndLightOn() ^ lightSystem.isLightAndLightOff() ^ lightSystem.isLightAndLightOn(),
                        lightSystem->{
                            seq(lightSystem.threshold(), Threshold::updateSensorReading);
                            seq(lightSystem.light(), Light::skip);
                            seq(lightSystem, LightSystem::updateLight);
                        })
        );
    }
    // dont think we need the other kases here as at the root object if theres no match nothing will get triggered, hence its a skip already,
    // however we should explicilty handle all kases...? is it good enough just to say, if night and off change to on, and if day and on, change to off, i think so
    static void updateLight(LightSystem l){
        kases(l,
                kase(lightSystem->lightSystem.isDarkAndLightOff(), lightSystem->lightSystem.isDarkAndLightOn(), lightSystem->{
                    seq(lightSystem.threshold(), Threshold::skipIfDark); // dont print skips to controlled english, requires update
                    seq(lightSystem.light(), Light::lightOn);
                }),
                kase(lightSystem->lightSystem.isDarkAndLightOn(), lightSystem->lightSystem.isDarkAndLightOn(), lightSystem->{
                    System.out.println("Is dark and light already on, hence no change.");
                }),
                kase(lightSystem->lightSystem.isLightAndLightOn(), lightSystem->lightSystem.isLightAndLightOff(), lightSystem->{
                    seq(lightSystem.threshold(), Threshold::skipIfLight); // dont print skips to controlled english, requires update
                    seq(lightSystem.light(), Light::lightOff);
                }),
                kase(lightSystem->lightSystem.isLightAndLightOff(), lightSystem->lightSystem.isLightAndLightOff(), lightSystem->{
                    System.out.println("Is light and light already off, hence no change.");
                })
        );
    }
}
