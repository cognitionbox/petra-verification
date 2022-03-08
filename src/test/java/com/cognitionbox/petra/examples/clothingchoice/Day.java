package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.lang.primitives.impls.PBoolean;

@Primative public interface Day {
    PBoolean isWeekend();
    default void setWeekend(){
        isWeekend().set(true);
    }
    default void setWeekday(){
        isWeekend().set(false);
    }
    default boolean weekend(){
        return isWeekend().get();
    }
    default boolean weekday(){
        return !isWeekend().get();
    }
}
