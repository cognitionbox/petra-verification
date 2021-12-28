package com.cognitionbox.petra.examples.nesting;

import com.cognitionbox.petra.lang.primitives.impls.PString;

public class Foo implements SomeView2 {
    public Fee fee1;
    public Fee fee2;
    private PString value = new PString();
    public boolean a(){return value.get().equals("a");}
    public boolean b(){return value.get().equals("b");}
    public boolean c(){return value.get().equals("c");}
    boolean d(){return value.get().equals("d");}
    boolean e(){return value.get().equals("e");}
    boolean f(){return value.get().equals("f");}
    boolean g(){return value.get().equals("g");}
    boolean h(){return value.get().equals("h");}
    boolean i(){return b() && c();}
    public void set(String v){ this.value.set(v);}

    @Override
    public FeeView fee1() {
        return fee1; // need a check for this
    }

    @Override
    public FeeView fee2() {
        return fee2; // need a check for this
    }
}
