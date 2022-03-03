package com.cognitionbox.petra.examples.algorithmictrading.marketdata;

import com.cognitionbox.petra.lang.primitives.impls.PBigDecimal;

public class QuoteImpl implements Quote {
    private final PBigDecimal bid = new PBigDecimal();
    private final PBigDecimal ask = new PBigDecimal();
    @Override
    public PBigDecimal bid() {
        return bid;
    }

    @Override
    public PBigDecimal ask() {
        return ask;
    }
}
