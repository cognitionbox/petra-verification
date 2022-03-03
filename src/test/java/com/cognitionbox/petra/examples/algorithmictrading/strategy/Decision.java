package com.cognitionbox.petra.examples.algorithmictrading.strategy;

import com.cognitionbox.petra.lang.primitives.impls.PBoolean;
import com.cognitionbox.petra.lang.primitives.impls.PBigDecimal;
import com.cognitionbox.petra.lang.primitives.impls.PString;

public interface Decision {
    PBigDecimal bid();
    PBigDecimal ask();
    PString symbol();
    PBoolean isOpen();
    PBoolean isLong();
    PBigDecimal qty();

    default void buy(){
        isLong().set(true);
        isOpen().set(true);
    }

    default void sell(){
        isLong().set(false);
        isOpen().set(true);
    }


    default void close(){
        isOpen().set(false);
    }

    default void hold(){
        isOpen().set(null);
    }

    default boolean notOpen(){
        return isOpen().get()==null || !isOpen().get();
    }

    default boolean open(){
        return isOpen().get();
    }
}
