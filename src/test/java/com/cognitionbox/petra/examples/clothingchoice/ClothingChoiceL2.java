package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.lang.step.PEdge;

import static com.cognitionbox.petra.lang.Petra.*;

public interface ClothingChoiceL2 extends PEdge<SystemStates> {

    static void accept(SystemStates s) {
        kases(s,
                kase(system -> system.rainyWeekdayUndecidedClothing(),
                        system -> system.rainyWeekdayCoatClothing(),
                        system -> {
                            system.clothing().choiceEnum().set(ClothingEnum.COAT);
                            printChoice(system.dayAndWeather(),system.clothing());
                        }),
                kase(system -> system.rainyWeekendUndecidedClothing(),
                        system -> system.rainyWeekendCoatClothing(),
                        system -> {
                            system.clothing().choiceEnum().set(ClothingEnum.COAT);
                            printChoice(system.dayAndWeather(),system.clothing());
                        }),
                kase(system -> system.plainWeekendUndecidedClothing(),
                        system -> system.plainWeekendTshirtClothing(),
                        system -> {
                            system.clothing().choiceEnum().set(ClothingEnum.T_SHIRT);
                            printChoice(system.dayAndWeather(),system.clothing());
                        }),
                kase(system -> system.notRainyWeekdayUndecidedClothing(),
                        system -> system.notRainyWeekdaySuitClothing(),
                        system -> {
                            system.clothing().choiceEnum().set(ClothingEnum.SUIT);
                            printChoice(system.dayAndWeather(),system.clothing());
                        }),
                kase(system -> system.sunnyWeekendUndecidedClothing(),
                        system -> system.sunnyWeekendHatClothing(),
                        system -> {
                            system.clothing().choiceEnum().set(ClothingEnum.HAT);
                            printChoice(system.dayAndWeather(),system.clothing());
                        })
        );
    }

    static void printChoice(DayAndWeather dw, Clothing c){
        System.out.println("is weekend?: "+dw.day().weekend());
        System.out.println("weather: "+dw.weather().weatherEnum().get());
        System.out.println("choice: "+c.choiceEnum().get());
    }

}
