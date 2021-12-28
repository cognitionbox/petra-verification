package com.cognitionbox.petra.examples.nesting;

public class FooMain {
    public static void main(String[] args) {
        new G1().accept(new Foo());
    }
}
