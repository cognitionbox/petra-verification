package com.cognitionbox.petra.examples.nesting;

public interface FeeView {
    FiiView fii1();
    FiiView fii2();
    // {a,b,c,d} X {a,b,c,d}

    default boolean a(){return fii1().a() || fii1().b();}
    default boolean b(){return fii1().c() || fii1().d();}

}
