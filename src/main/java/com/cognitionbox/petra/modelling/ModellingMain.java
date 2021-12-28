package com.cognitionbox.petra.modelling;

import static com.cognitionbox.petra.modelling.Modelling.*;

public class ModellingMain {
    public static void main(String[] args){
        Sys s = system("w");
        // i think all states them selfs need to be abtractions, which the symbolic state("a") for e.g. being a single object, with arrow to itself
        State x = xor(state("a"),state("b"),state("c")); // xor is abstraction of containing states to the name (a xor b)
        State y = xor(state("d"),state("e"));
        State z = x.product(y); // is abstraction of the states of product to the name (x product y)

        // this means all states ie abstractions can be rendered as abstractions

        SAbs abs1 = x.abs(y,p->p.symbol=="a" || p.symbol=="b",q->q.symbol=="c");
        SAbs abs2 = y.abs(state("someName"),p->true); // single name abstraction
        s.add(x,y,z);
        s.add(abs1);
        s.add(abs2);
        System.out.println(z);
        System.out.println(s.verify());
    }
}
