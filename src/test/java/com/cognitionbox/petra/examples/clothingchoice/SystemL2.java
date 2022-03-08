package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.View;

@View public interface SystemL2 extends SystemBase {
    Clothing clothing();
    DayAndWeather dayAndWeather();

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
