package com.cognitionbox.petra.examples.nesting;

public class Fii implements FiiView {
    final Foo foo1 = new Foo();
    final Foo foo2 = new Foo(); // need to refer to this too
    @Override
    public Foo foo1() {
        return foo1;
    }

    @Override
    public Foo foo2() {
        return foo2;
    }
}
