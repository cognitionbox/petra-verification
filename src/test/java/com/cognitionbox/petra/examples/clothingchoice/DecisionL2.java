package com.cognitionbox.petra.examples.clothingchoice;

public interface DecisionL2 extends DecisionL1 {
    default boolean clothingUndecidedRainyWeekday(){return clothing().undecided() && dayAndWeather().rainyWeekday();}
    default boolean clothingUndecidedRainyWeekend(){return clothing().undecided() && dayAndWeather().rainyWeekend();};
    default boolean clothingUndecidedPlainWeekend(){return dayAndWeather().plainWeekend() && clothing().undecided();};
    default boolean clothingUndecidedNotRainyWeekday(){return dayAndWeather().notRainyWeekday() && clothing().undecided();};
    default boolean clothingUndecidedSunnyWeekend(){return dayAndWeather().notRainyWeekday() && clothing().undecided();};
}
