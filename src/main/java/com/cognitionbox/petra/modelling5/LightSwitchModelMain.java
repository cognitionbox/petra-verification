package com.cognitionbox.petra.modelling5;

import static com.cognitionbox.petra.modelling5.MODELLING.*;

public class LightSwitchModelMain {
    public static void main(String[] args){
        SYSTEM system = system("TRADING");

        STATE switchActive = state("switchActive");
        STATE switchNotActive = state("switchNotActive");
        STATE switchState = xor(switchActive,switchNotActive);

        STATE powerOn = state("powerOn");
        STATE powerOff = state("powerOff");
        STATE powerState = xor(powerOn,powerOff);

        STATE light = switchState.with(powerState);

        STATE lightOn = state("lightOn");
        STATE lightOff = state("lightOff");
        STATE lightState = xor(lightOn,lightOff);

        // NotActive
        //ABS abs = light.abs(lightState,a->a.symbol.equals("switchActive & powerOn"),b->b.symbol.contains("off") || b.symbol.contains("off"));
        ABS abs = light.abs(lightState,a->a.symbol.equals("switchActive & powerOn")); // ,b->b.symbol.contains("powerOff") || b.symbol.contains("switchNotActive")

        system.add(light);
        system.add(abs);
        system.renderStates();
        System.out.println(system.verifyStates());
    }
}
