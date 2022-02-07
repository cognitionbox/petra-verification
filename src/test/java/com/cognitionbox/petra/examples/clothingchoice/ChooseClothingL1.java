package com.cognitionbox.petra.examples.clothingchoice;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

public class ChooseClothingL1 implements Consumer<SystemL1> {
    @Override
    public void accept(SystemL1 s) {
        kases(s,
                kase(system->system.clothingUndecided(),
                    system->system.sunnyWeekdayHatClothing() ^
                            system.rainyWeekdaySmartJacketClothing() ^
                            system.moderateWeekdayAnyClothing() ^
                            system.sunnyWeekendTshirtClothing() ^
                            system.rainyWeekendRainCoatClothing() ^
                            system.moderateWeekendNotSmartJacketClothing(),
                    system->{
                        seq((SystemL2) system,new ChooseClothingL2());
                    })
                );
    }
}
