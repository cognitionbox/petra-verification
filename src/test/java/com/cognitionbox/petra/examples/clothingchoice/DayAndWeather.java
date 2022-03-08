package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.View;

@View
public interface DayAndWeather {
    Day day();
    Weather weather();

    default boolean sunnyWeekend(){
        return day().weekend() && weather().sunny();
    }

    default boolean rainyWeekend(){
        return day().weekend() && weather().rainy();
    }

    default boolean plainWeekend(){
        return day().weekend() && weather().plain();
    }

    default boolean rainyWeekday(){
        return day().weekday() && weather().rainy();
    }

    default boolean notRainyWeekday(){
        return !weather().rainy() && day().weekday();
    }
}
