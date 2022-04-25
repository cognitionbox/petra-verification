package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.examples.clothingchoice.impl.ClothingEnum;
import com.cognitionbox.petra.lang.primitives.PValue;

@Primative @View
public interface Clothing {
    PValue<ClothingEnum> choiceEnum();

    default  boolean undecided(){
        return choiceEnum().get()==null;
    }
    default boolean suit(){
        return this.choiceEnum().get()==ClothingEnum.SUIT;
    }
    default boolean coat(){
        return this.choiceEnum().get()==ClothingEnum.COAT;
    }
    default boolean Tshirt(){
        return this.choiceEnum().get()==ClothingEnum.T_SHIRT;
    }
    default boolean hat(){
        return this.choiceEnum().get()==ClothingEnum.HAT;
    }

}
