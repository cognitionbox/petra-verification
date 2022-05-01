package com.cognitionbox.petra.examples.coffeemachine;


import com.cognitionbox.petra.examples.clothingchoice.ClothingChoiceL1;
import com.cognitionbox.petra.examples.clothingchoice.SystemStates;
import com.cognitionbox.petra.lang.Petra;

public class CoffeeMachineMain {
    public static void main(String... args){
        Petra.start(ProcessCoffee::process, CoffeeMachine.class);
    }
}
