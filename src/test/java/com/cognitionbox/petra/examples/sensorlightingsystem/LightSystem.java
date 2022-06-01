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
                        lightSystem->true,lightSystem->true,
                        lightSystem->{
                            seq(lightSystem.threshold(), Threshold::updateSensorReading);
                            seq(lightSystem.light(), Light::skip);
                            seq(lightSystem, LightSystem::updateLight);
                        })
        );
    }

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
