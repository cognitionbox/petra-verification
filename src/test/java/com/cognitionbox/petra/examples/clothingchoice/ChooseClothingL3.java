package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class ChooseClothingL3 implements Consumer<SystemL2> {
    @Override
    public void accept(SystemL2 s) {
        kases(s,
                kase(system -> system.sunnyWeekdayUndecidedClothing(),
                        system -> system.sunnyWeekdayHatClothing(),
                        system -> {
                            //
                        }),
                kase(system -> system.rainyWeekdayUndecidedClothing(),
                        system -> system.rainyWeekdaySmartJacketClothing(),
                        system -> {
                            //
                        }),
                kase(system -> system.moderateWeekdayUndecidedClothing(),
                        system -> system.moderateWeekdayUndecidedClothing(),
                        system -> {
                            //
                        })
        );
    }
}
