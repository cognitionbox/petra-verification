package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.lang.step.PEdge;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;


public class ChooseClothingL3 implements PEdge<SystemL2> {
    @Override
    public void accept(SystemL2 s) {
        kases(s,
                kase(system -> system.rainyWeekdayUndecidedClothing(),
                        system -> system.rainyWeekdayCoatClothing(),
                        system -> {
                            system.clothing().chooseCoat();
                            system.printChoice();
                        }),
                kase(system -> system.rainyWeekendUndecidedClothing(),
                        system -> system.rainyWeekendCoatClothing(),
                        system -> {
                            system.clothing().chooseCoat();
                            system.printChoice();
                        }),
                kase(system -> system.plainWeekendUndecidedClothing(),
                        system -> system.plainWeekendTshirtClothing(),
                        system -> {
                            system.clothing().chooseTshirt();
                            system.printChoice();
                        }),
                kase(system -> system.notRainyWeekdayUndecidedClothing(),
                        system -> system.notRainyWeekdaySuitClothing(),
                        system -> {
                            system.clothing().chooseSuit();
                            system.printChoice();
                        }),
                kase(system -> system.sunnyWeekendUndecidedClothing(),
                        system -> system.sunnyWeekendHatClothing(),
                        system -> {
                            system.clothing().chooseHat();
                            system.printChoice();
                        })
        );
    }
}
