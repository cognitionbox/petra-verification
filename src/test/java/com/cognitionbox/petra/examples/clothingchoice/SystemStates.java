package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.View;

@View public interface SystemStates {
    Clothing clothing();
    DayAndWeather dayAndWeather();

    default boolean clothingUndecided(){
        return clothing().undecided();
    }

    default boolean rainyWeekdayCoatClothing(){
        return clothing().coat() && dayAndWeather().rainyWeekday();
    }

    default boolean rainyWeekendCoatClothing(){
        return clothing().coat() && dayAndWeather().rainyWeekend();
    }

    default boolean plainWeekendTshirtClothing(){
        return dayAndWeather().plainWeekend() && clothing().Tshirt();
    }

    default boolean notRainyWeekdaySuitClothing(){
        return dayAndWeather().notRainyWeekday() && clothing().suit();
    }

    default boolean sunnyWeekendHatClothing(){
        return dayAndWeather().sunnyWeekend() && clothing().hat();
    }

    default boolean rainyWeekdayUndecidedClothing(){
        return clothing().undecided() && dayAndWeather().rainyWeekday();
    }

    default boolean rainyWeekendUndecidedClothing(){
        return clothing().undecided() && dayAndWeather().rainyWeekend();
    }

    default boolean plainWeekendUndecidedClothing(){
        return dayAndWeather().plainWeekend() && clothing().undecided();
    }

    default boolean notRainyWeekdayUndecidedClothing(){
        return dayAndWeather().notRainyWeekday() && clothing().undecided();
    }

    default boolean sunnyWeekendUndecidedClothing(){
        return dayAndWeather().sunnyWeekend() && clothing().undecided();
    }

}
