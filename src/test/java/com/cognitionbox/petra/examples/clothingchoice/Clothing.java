package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.lang.primitives.PValue;

@Primative
public interface Clothing {
    PValue<ClothingEnum> choice();
    default boolean undecided(){
        return this.choice().get()==ClothingEnum.U;
    }
    default boolean smartJacket(){
        return this.choice().get()==ClothingEnum.L;
    }
    default boolean rainCoat(){
        return this.choice().get()==ClothingEnum.R;
    }
    default boolean Tshirt(){
        return this.choice().get()==ClothingEnum.C;
    }

    default boolean hat(){
        return this.choice().get()==ClothingEnum.H;
    }
}
