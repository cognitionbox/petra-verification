package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

@Edge public class ChooseClothingL2 implements Consumer<SystemL2> {
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
                        seq((SystemImpl)system, new ChooseClothingL3());
                    })
                );
    }
}
