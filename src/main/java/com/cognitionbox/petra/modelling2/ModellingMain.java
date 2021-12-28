package com.cognitionbox.petra.modelling2;

import static com.cognitionbox.petra.modelling2.Modelling.*;

public class ModellingMain {
    public static void main(String[] args){
        Sys s = system("w");
        // i think all states them selfs need to be abtractions, which the symbolic state("a") for e.g. being a single object, with arrow to itself
        XOR x = xor(state("a"),state("b"),state("c")); // xor is abstraction of containing states to the name (a xor b)
        XOR y = xor(state("d"),state("e"));
        AND z = and(x,y); // is abstraction of the states of product to the name (x product y)
        AND w = and(state("p"),state("q"));
        // this means all states ie abstractions can be rendered as abstractions

        XOR abs1 = x.too().abs(y.from(), p->p.symbol=="a" || p.symbol=="b", q->q.symbol=="c");
        XOR abs2 = y.too().abs(state("someName").from(), p->true); // single name abstraction
//        State tmp = state("z");
//        XOR stateAbs = tmp.abs(tmp, p->true);
        //s.add(x,y);
        s.add(x);
        s.add(y);
        s.add(abs1);
        s.add(abs2);
        //s.add(stateAbs);
        s.add(w);
        s.add(z);
        System.out.println(z.from);
        System.out.println(z.too);
        System.out.println(s.verify());
    }
}
