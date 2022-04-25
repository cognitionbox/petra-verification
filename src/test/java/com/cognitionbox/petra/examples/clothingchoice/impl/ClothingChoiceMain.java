package com.cognitionbox.petra.examples.clothingchoice.impl;


import com.cognitionbox.petra.examples.clothingchoice.ClothingChoiceL1;
import com.cognitionbox.petra.examples.clothingchoice.SystemStates;
import com.cognitionbox.petra.lang.Petra;

public class ClothingChoiceMain {
    public static void main(String... args){
        Petra.start(ClothingChoiceL1::main, SystemStates.class);
    }
}
