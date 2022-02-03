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

    default boolean moderateWeekend(){
        return day().weekend() && weather().moderate();
    }

    default boolean sunnyWeekday(){
        return day().weekday() && weather().sunny();
    }

    default boolean rainyWeekday(){
        return day().weekday() && weather().rainy();
    }

    default boolean moderateWeekday(){
        return day().weekday() && weather().moderate();
    }
}
