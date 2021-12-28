package com.cognitionbox.petra.examples.mainexample;

public class Main {

    public static void main(String[] args) {

        Foo foo = new Foo();
        while(true){
            new A().accept(foo);
        }
    }
}
