package com.cognitionbox.petra.examples.mainexample;

import com.cognitionbox.petra.lang.primitives.impls.PString;

import java.util.Collection;

public class Foo {
    public Fee fee1;
    public Fee fee2;
    public Collection<Fee> list;
    private PString value = new PString();
    public boolean a(){return value.get().equals("a");}
    public boolean b(){return value.get().equals("b");}
    public boolean c(){return value.get().equals("c");}
    public boolean d(){return value.get().equals("d");}
    boolean e(){return value.get().equals("e");}
    boolean f(){return value.get().equals("f");}
    boolean g(){return value.get().equals("g");}
    boolean h(){return value.get().equals("h");}
    public void set(String v){ this.value.set(v);}
}
