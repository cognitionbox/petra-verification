package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.lang.primitives.PValue;

public class ClothingImpl implements Clothing {
    PValue<ClothingEnum> choice = new PValue<>();
    @Override
    public PValue<ClothingEnum> choice() {
        return choice;
    }
}
