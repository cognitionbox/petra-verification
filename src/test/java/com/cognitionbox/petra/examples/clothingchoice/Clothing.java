package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.lang.primitives.PValue;

@Primative
public interface Clothing {
    PValue<ClothingEnum> choiceEnum();
    default void chooseSuit(){choiceEnum().set(ClothingEnum.SUIT);}
    default void chooseCoat(){choiceEnum().set(ClothingEnum.COAT);}
    default void chooseTshirt(){choiceEnum().set(ClothingEnum.T_SHIRT);}
    default void chooseHat(){choiceEnum().set(ClothingEnum.HAT);}
    default boolean undecided(){
        return this.choiceEnum().get()==ClothingEnum.UNDECIDED;
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

    default void setUndecided(){
        choiceEnum().set(ClothingEnum.UNDECIDED);
    }
}
