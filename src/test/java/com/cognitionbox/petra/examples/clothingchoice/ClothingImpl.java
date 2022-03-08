package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.lang.primitives.PValue;

public class ClothingImpl implements Clothing {
    PValue<ClothingEnum> choiceEnum = new PValue<>();
    @Override
    public PValue<ClothingEnum> choiceEnum() {
        return choiceEnum;
    }
}
