package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.*;

@View
public interface ClothingChoiceL1 extends SystemStates {

    static void main(SystemStates s){
        kases(s,
                kase(systemStates->systemStates.clothing().undecided(),
                        systemStates->
                                (systemStates.clothing().coat() && systemStates.dayAndWeather().rainyWeekday()) ^
                                (systemStates.clothing().coat() && systemStates.dayAndWeather().rainyWeekend()) ^
                                (systemStates.dayAndWeather().plainWeekend() && systemStates.clothing().Tshirt()) ^
                                (systemStates.dayAndWeather().notRainyWeekday() && systemStates.clothing().suit()) ^
                                (systemStates.dayAndWeather().sunnyWeekend() && systemStates.clothing().hat()),
                        systemStates->{
                            seq(systemStates, ClothingChoiceL1::init);
                            seq(systemStates, ClothingChoiceL1::accept);
                        })
        );
    }

    @Edge static void init(SystemStates s){
        kases(s,
                kase(systemStates->systemStates.clothing().undecided(),
                    systemStates->systemStates.clothing().undecided() && systemStates.dayAndWeather().rainyWeekday(),
                    systemStates->{
                        systemStates.dayAndWeather().day().setWeekday();
                        systemStates.dayAndWeather().weather().setRainy();
                    })
        );
    }

    static void accept(SystemStates s) {
        kases(s,
                kase(systemStates -> (systemStates.clothing().undecided() && systemStates.dayAndWeather().rainyWeekday()) ^
                                (systemStates.clothing().undecided() && systemStates.dayAndWeather().rainyWeekend())  ^
                                (systemStates.dayAndWeather().plainWeekend() && systemStates.clothing().undecided())  ^
                                (systemStates.dayAndWeather().notRainyWeekday() && systemStates.clothing().undecided())  ^
                                (systemStates.dayAndWeather().sunnyWeekend() && systemStates.clothing().undecided()) ,
                        systemStates->
                                (systemStates.clothing().coat() && systemStates.dayAndWeather().rainyWeekday()) ^
                                (systemStates.clothing().coat() && systemStates.dayAndWeather().rainyWeekend()) ^
                                (systemStates.dayAndWeather().plainWeekend() && systemStates.clothing().Tshirt()) ^
                                (systemStates.dayAndWeather().notRainyWeekday() && systemStates.clothing().suit()) ^
                                (systemStates.dayAndWeather().sunnyWeekend() && systemStates.clothing().hat()),
                        systemStates -> {
                            seq(systemStates, ClothingChoiceL2::accept);
                        })
        );
    }

}
