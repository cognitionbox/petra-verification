package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.lang.primitives.PValue;

@Primative public interface Weather {
    PValue<WeatherEnum> weather();
    default boolean sunny(){
        return weather().get()==WeatherEnum.S;
    }
    default boolean rainy(){
        return weather().get()==WeatherEnum.R;
    }
    default boolean moderate(){
        return weather().get()==WeatherEnum.P;
    }
}
