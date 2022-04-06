package com.cognitionbox.petra.examples.nesting;

import static com.cognitionbox.petra.lang.Petra.start;

public class FooMain {
    public static void main(String[] args) {
        start(G1::accept,Foo.class);
    }
}
