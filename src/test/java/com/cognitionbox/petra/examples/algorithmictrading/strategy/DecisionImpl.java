package com.cognitionbox.petra.examples.algorithmictrading.strategy;

import com.cognitionbox.petra.lang.primitives.impls.PBoolean;
import com.cognitionbox.petra.lang.primitives.impls.PBigDecimal;
import com.cognitionbox.petra.lang.primitives.impls.PString;

public class DecisionImpl implements Decision {
    private final PString symbol = new PString();
    private final PBoolean isLong = new PBoolean();
    private final PBoolean isOpen = new PBoolean();
    private final PBigDecimal bid = new PBigDecimal();
    private final PBigDecimal ask = new PBigDecimal();
    private final PBigDecimal qty = new PBigDecimal();

    @Override
    public PBigDecimal bid() {
        return bid;
    }

    @Override
    public PBigDecimal ask() {
        return ask;
    }

    @Override
    public PString symbol() {
        return symbol;
    }

    @Override
    public PBoolean isLong() {
        return isLong;
    }

    @Override
    public PBoolean isOpen() {
        return isOpen;
    }

    @Override
    public PBigDecimal qty() {
        return qty;
    }
}
