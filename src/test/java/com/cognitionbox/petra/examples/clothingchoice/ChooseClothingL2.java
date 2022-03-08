package com.cognitionbox.petra.examples.clothingchoice;



import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.*;

public class ChooseClothingL2 implements PGraph<SystemL2> {
    @Override
    public void accept(SystemL2 s) {
        kases(s,
                kase(system->system.rainyWeekdayUndecidedClothing() ^
                                system.rainyWeekendUndecidedClothing() ^
                                system.plainWeekendUndecidedClothing() ^
                                system.notRainyWeekdayUndecidedClothing() ^
                                system.sunnyWeekendUndecidedClothing(),
                        system->system.rainyWeekdayCoatClothing() ^
                                system.rainyWeekendCoatClothing() ^
                                system.plainWeekendTshirtClothing() ^
                                system.notRainyWeekdaySuitClothing() ^
                                system.sunnyWeekendHatClothing(),
                    system->{
                        seq(system, new ChooseClothingL3());
                    })
                );
    }
}
