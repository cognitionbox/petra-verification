package com.cognitionbox.petra.examples.nesting2;

import com.cognitionbox.petra.examples.nesting.FeeView;
import com.cognitionbox.petra.examples.nesting.Fii;
import com.cognitionbox.petra.examples.nesting.FiiView;

public class Fee implements FeeView {
    final Fii fii1 = new Fii();
    final Fii fii2 = new Fii();
    // {a,b} X {a,b}
    //{(a,a),(a,b),(b,a),(b,b)}

    @Override
    public FiiView fii1() {
        return fii1;
    }

    @Override
    public FiiView fii2() {
        return fii2;
    }

    public boolean a(){return fii1.a() && fii2.b();} // {(a,a),(a,b)}
    public boolean b(){return fii1.b();} // {(b,a),(b,b)}

}
