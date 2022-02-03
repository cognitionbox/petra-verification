package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge public class ChooseClothingDetails implements Consumer<System> {
    @Override
    public void accept(System s) {
        kases(s,
                kase(
                    system->system.sunnyWeekdayUndecidedClothing() ^
                            system.rainyWeekdayUndecidedClothing() ^
                            system.moderateWeekdayUndecidedClothing() ^
                            system.sunnyWeekendUndecidedClothing() ^
                            system.rainyWeekendUndecidedClothing() ^
                            system.moderateWeekendUndecidedClothing(),
                        system->system.sunnyWeekdayHatClothing() ^
                                system.rainyWeekdaySmartJacketClothing() ^
                                system.moderateWeekdayUndecidedClothing() ^
                                system.sunnyWeekendTshirtClothing() ^
                                system.rainyWeekendRainCoatClothing() ^
                                system.moderateWeekendNotSmartJacketClothing(),
                    system->{
                        //
                    })
                );
    }
}
