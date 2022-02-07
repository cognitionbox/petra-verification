package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.View;

@View public interface SystemL2 extends SystemBase {
    Clothing clothing();
    DayAndWeather dayAndWeather();

    default boolean sunnyWeekdayUndecidedClothing(){
        return dayAndWeather().sunnyWeekday() && clothing().undecided();
    }

    default boolean rainyWeekdayUndecidedClothing(){
        return dayAndWeather().rainyWeekday() && clothing().undecided();
    }

    default boolean moderateWeekdayUndecidedClothing(){
        return dayAndWeather().moderateWeekday() && clothing().undecided();
    }


    default boolean sunnyWeekendUndecidedClothing(){
        return dayAndWeather().sunnyWeekend() && clothing().undecided();
    }

    default boolean rainyWeekendUndecidedClothing(){
        return dayAndWeather().rainyWeekend() && clothing().undecided();
    }

    default boolean moderateWeekendUndecidedClothing(){
        return dayAndWeather().moderateWeekend() && clothing().undecided();
    }


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
