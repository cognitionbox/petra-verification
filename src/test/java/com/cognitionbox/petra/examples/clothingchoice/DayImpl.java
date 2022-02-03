package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.lang.primitives.impls.PBoolean;

public class DayImpl implements Day {
    private PBoolean weekend = new PBoolean();
    @Override
    public PBoolean isWeekend() {
        return weekend;
    }
}
