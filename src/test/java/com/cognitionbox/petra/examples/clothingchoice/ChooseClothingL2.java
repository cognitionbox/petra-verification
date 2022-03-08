package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.*;

public class ChooseClothingL2 implements PGraph<SystemL2> {
    @Override
    public void accept(SystemL2 s) {
        kases(s,
                kase(system->system.sunnyWeekdayUndecidedClothing() ^
                            system.rainyWeekdayUndecidedClothing() ^
                            system.moderateWeekdayUndecidedClothing() ^
                            system.sunnyWeekendUndecidedClothing() ^
                            system.rainyWeekendUndecidedClothing() ^
                            system.moderateWeekendUndecidedClothing(),
                    system->system.sunnyWeekdayHatClothing() ^
                            system.rainyWeekdaySmartJacketClothing() ^
                            system.moderateWeekdayAnyClothing() ^
                            system.sunnyWeekendTshirtClothing() ^
                            system.rainyWeekendRainCoatClothing() ^
                            system.moderateWeekendNotSmartJacketClothing(),
                    system->{
                        seq(system, new ChooseClothingL3());
                    })
                );
    }
}
