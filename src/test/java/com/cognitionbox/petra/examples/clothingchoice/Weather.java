package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.lang.primitives.PValue;

@Primative public interface Weather {
    PValue<WeatherEnum> weatherEnum();
    default void setSunny(){weatherEnum().set(WeatherEnum.SUNNY);}
    default void setRainy(){weatherEnum().set(WeatherEnum.RAINY);}
    default void setPlain(){weatherEnum().set(WeatherEnum.PLAIN);}
    default boolean sunny(){
        return weatherEnum().get()==WeatherEnum.SUNNY;
    }
    default boolean rainy(){
        return weatherEnum().get()==WeatherEnum.RAINY;
    }
    default boolean plain(){
        return weatherEnum().get()==WeatherEnum.PLAIN;
    }
}
