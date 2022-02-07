package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.Primative;

// Base views should not be analyzed, need a new tag like @Base
@Primative public interface SystemBase {
    Clothing clothing();
    DayAndWeather dayAndWeather();

    default boolean sunnyWeekendTshirtClothing(){
        return dayAndWeather().sunnyWeekend() && clothing().Tshirt();
    }

    default boolean rainyWeekendRainCoatClothing(){
        return dayAndWeather().rainyWeekend() && clothing().rainCoat();
    }

    default boolean moderateWeekendNotSmartJacketClothing(){
        return dayAndWeather().moderateWeekend() && !clothing().smartJacket() && !clothing().undecided();
    }

    default boolean sunnyWeekdayHatClothing(){
        return dayAndWeather().sunnyWeekday() && clothing().hat();
    }

    default boolean rainyWeekdaySmartJacketClothing(){
        return clothing().smartJacket() && (dayAndWeather().rainyWeekday() && dayAndWeather().moderateWeekday());
    }

    default boolean moderateWeekdayAnyClothing(){
        return dayAndWeather().moderateWeekday() && !clothing().undecided();
    }
}
