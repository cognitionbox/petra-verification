package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.examples.clothingchoice.impl.ClothingEnum;
import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.*;

@View
public interface ClothingChoiceL2 extends SystemStates {

    @Edge static void accept(SystemStates s) {
        kases(s,
                kase(systemStates -> systemStates.clothing().undecided() && systemStates.dayAndWeather().rainyWeekday(),
                        systemStates -> systemStates.clothing().coat() && systemStates.dayAndWeather().rainyWeekday(),
                        systemStates -> {
                            systemStates.clothing().choiceEnum().set(ClothingEnum.COAT);
                            printChoice(systemStates.dayAndWeather(),systemStates.clothing());
                        }),
                kase(systemStates -> systemStates.clothing().undecided() && systemStates.dayAndWeather().rainyWeekend(),
                        systemStates -> systemStates.clothing().coat() && systemStates.dayAndWeather().rainyWeekend(),
                        systemStates -> {
                            systemStates.clothing().choiceEnum().set(ClothingEnum.COAT);
                            printChoice(systemStates.dayAndWeather(),systemStates.clothing());
                        }),
                kase(systemStates -> systemStates.dayAndWeather().plainWeekend() && systemStates.clothing().undecided(),
                        systemStates -> systemStates.dayAndWeather().plainWeekend() && systemStates.clothing().Tshirt(),
                        systemStates -> {
                            systemStates.clothing().choiceEnum().set(ClothingEnum.T_SHIRT);
                            printChoice(systemStates.dayAndWeather(),systemStates.clothing());
                        }),
                kase(systemStates -> systemStates.dayAndWeather().notRainyWeekday() && systemStates.clothing().undecided() ,
                        systemStates -> systemStates.dayAndWeather().notRainyWeekday() && systemStates.clothing().suit(),
                        systemStates -> {
                            systemStates.clothing().choiceEnum().set(ClothingEnum.SUIT);
                            printChoice(systemStates.dayAndWeather(),systemStates.clothing());
                        }),
                kase(systemStates -> systemStates.dayAndWeather().sunnyWeekend() && systemStates.clothing().undecided(),
                        systemStates -> systemStates.dayAndWeather().sunnyWeekend() && systemStates.clothing().hat(),
                        systemStates -> {
                            systemStates.clothing().choiceEnum().set(ClothingEnum.HAT);
                            printChoice(systemStates.dayAndWeather(),systemStates.clothing());
                        })
        );
    }

    static void printChoice(DayAndWeather dw, Clothing c){
        System.out.println("is weekend?: "+dw.day().weekend());
        System.out.println("weather: "+dw.weather().weatherEnum().get());
        System.out.println("choice: "+c.choiceEnum().get());
    }

}
