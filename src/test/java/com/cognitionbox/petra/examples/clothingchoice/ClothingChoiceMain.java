package com.cognitionbox.petra.examples.clothingchoice;

import static com.cognitionbox.petra.lang.Petra.start;

public class ClothingChoiceMain {
    public static void main(String... args){
        SystemL1 systemL1 = new SystemImpl();
        systemL1.dayAndWeather().day().setWeekday();
        systemL1.dayAndWeather().weather().setRainy();
        systemL1.clothing().setUndecided();
        start(new ChooseClothingL1(),systemL1);
    }
}
