package com.cognitionbox.petra.examples.nesting2;

import com.cognitionbox.petra.lang.primitives.impls.PInteger;

public class Fum {
    private final PInteger i = new PInteger(0);
    public boolean a(){ return i.ge(new PInteger(0)) && i.lt(new PInteger(1000)); }
    public boolean b(){ return i.ge(new PInteger(1000)) && i.lt(new PInteger(2000)); }
    public boolean c(){ return i.ge(new PInteger(2000)) && i.lt(new PInteger(3000)); }
    public boolean d(){ return i.ge(new PInteger(3000)) && i.lt(new PInteger(3000)); }
}
