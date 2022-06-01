package com.cognitionbox.petra.examples.clothingchoice;

public interface DecisionL1 extends SystemStates {
    default boolean clothingUndecided(){return clothing().undecided();}
    default boolean coatRainyWeekday(){return clothing().coat() && dayAndWeather().rainyWeekday();}
    default boolean coatRainyWeekend(){return clothing().coat() && dayAndWeather().rainyWeekend();};
    default boolean tshirtPlainWeekend(){return dayAndWeather().plainWeekend() && clothing().Tshirt();};
    default boolean suitNotRainyWeekday(){return dayAndWeather().notRainyWeekday() && clothing().suit();};
    default boolean hatSunnyWeekend(){return dayAndWeather().notRainyWeekday() && clothing().suit();};
}
