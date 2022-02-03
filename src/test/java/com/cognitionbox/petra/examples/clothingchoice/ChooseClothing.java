package com.cognitionbox.petra.examples.clothingchoice;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

public class ChooseClothing implements Consumer<System> {
    @Override
    public void accept(System s) {
        kases(s,
                kase(
                    system->system.clothing().undecided(),
                    system->system.sunnyWeekdayHatClothing() ^
                            system.rainyWeekdaySmartJacketClothing() ^
                            system.moderateWeekdayUndecidedClothing() ^
                            system.sunnyWeekendTshirtClothing() ^
                            system.rainyWeekendRainCoatClothing() ^
                            system.moderateWeekendNotSmartJacketClothing(),
                    system->{
                        seq(system,new ChooseClothingDetails());
                    })
                );
    }
}
