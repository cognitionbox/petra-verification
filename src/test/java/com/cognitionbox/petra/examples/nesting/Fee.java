package com.cognitionbox.petra.examples.nesting;

public class Fee implements FeeView {
    FiiView fii1 = new Fii();
    FiiView fii2 = new Fii();
    @Override
    public FiiView fii1() {
        return fii1;
    }

    @Override
    public FiiView fii2() {
        return fii2;
    }
}
