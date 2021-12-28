package com.cognitionbox.petra.examples.turingmachine;

public interface StateView {
    StateValue state();
    StateValue headValue();

    default boolean stateAandTapeHeadIsA(){
        return state()==StateValue.A && headValue()==StateValue.A;
    }

    default boolean stateAandTapeHeadIsB(){
        return state()==StateValue.A && headValue()==StateValue.B;
    }

    default boolean stateBandTapeHeadIsA(){
        return state()==StateValue.B && headValue()==StateValue.A;
    }

    default boolean stateBandTapeHeadIsB(){
        return state()==StateValue.B && headValue()==StateValue.B;
    }
}
