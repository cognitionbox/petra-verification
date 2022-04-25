package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.*;

public interface ClothingChoiceL1 extends PGraph<SystemStates> {

    static void main(SystemStates s){
        kases(s,
                kase(system->system.clothingUndecided(),
                        system->system.rainyWeekdayCoatClothing() ^
                                system.rainyWeekendCoatClothing() ^
                                system.plainWeekendTshirtClothing() ^
                                system.notRainyWeekdaySuitClothing() ^
                                system.sunnyWeekendHatClothing(),
                        system->{
                            seq(system, ClothingChoiceL1::init);
                            seq(system, ClothingChoiceL1::accept);
                        })
        );
    }

    @Edge static void init(SystemStates s){
        kases(s,
                kase(system->system.clothingUndecided(),
                    system->system.rainyWeekdayUndecidedClothing(),
                    system->{
                        system.dayAndWeather().day().setWeekday();
                        system.dayAndWeather().weather().setRainy();
                    })
        );
    }

    static void accept(SystemStates s) {
        kases(s,
                kase(system -> system.rainyWeekdayUndecidedClothing() ^
                                system.rainyWeekendUndecidedClothing() ^
                                system.plainWeekendUndecidedClothing() ^
                                system.notRainyWeekdayUndecidedClothing() ^
                                system.sunnyWeekendUndecidedClothing(),
                        system -> system.rainyWeekdayCoatClothing() ^
                                system.rainyWeekendCoatClothing() ^
                                system.plainWeekendTshirtClothing() ^
                                system.notRainyWeekdaySuitClothing() ^
                                system.sunnyWeekendHatClothing(),
                        system -> {
                            seq(system, ClothingChoiceL2::accept);
                        })
        );
    }

}
