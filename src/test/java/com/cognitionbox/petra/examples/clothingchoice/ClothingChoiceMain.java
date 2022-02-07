package com.cognitionbox.petra.examples.clothingchoice;

import static com.cognitionbox.petra.lang.Petra.start;

public class ClothingChoiceMain {
    public static void main(String... args){
        start(new ChooseClothingL1(),new SystemImpl());
    }
}
