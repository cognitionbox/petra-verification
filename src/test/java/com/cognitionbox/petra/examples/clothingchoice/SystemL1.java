package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.View;

@View public interface SystemL1 extends SystemBase {
    Clothing clothing();
    DayAndWeather dayAndWeather();

    default boolean clothingUndecided(){
        return clothing().undecided();
    }
}
