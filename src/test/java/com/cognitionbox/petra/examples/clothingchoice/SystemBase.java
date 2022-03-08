package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.Primative;

// Base views should not be analyzed, need a new tag like @Base
@Primative public interface SystemBase {
    Clothing clothing();
    DayAndWeather dayAndWeather();

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

    default void printChoice(){
        System.out.println("is weekend?: "+dayAndWeather().day().weekend());
        System.out.println("weather: "+dayAndWeather().weather().weatherEnum().get());
        System.out.println("choice: "+clothing().choiceEnum().get());
    }


}
