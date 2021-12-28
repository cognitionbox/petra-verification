package com.cognitionbox.petra.modelling5;

import static com.cognitionbox.petra.modelling5.MODELLING.*;

public class ModellingMain {
    public static void main(String[] args){
        SYSTEM s = system("w");
        // i think all states them selfs need to be abtractions, which the symbolic state("a") for e.g. being a single object, with arrow to itself
        STATE x = xor("a","b","c"); // xor is abstraction of containing states to the name (a xor b)
        STATE y = xor("d","e");
        STATE z = x.with(y); // is abstraction of the states of product to the name (x product y)
        STATE r = state("r");
        STATE u = z.with(r);
        // this means all states ie abstractions can be rendered as abstractions
        ABS abs3 = z.abs(xor(state("i"),state("j")), p->p.symbol=="c" || p.symbol=="b", q->q.symbol=="a");
        ABS abs1 = x.abs(y, p->p.symbol=="a" || p.symbol=="b", q->q.symbol=="c");
        STATE someName = state("someName");
        STATE someProduct = z.with(someName);
        ABS abs2 = y.abs(someName, p->true); // single name abstraction

        STATE g = state("g");
        STATE h = state("h");
        STATE a = state("x");
        STATE b = state("y");
        STATE k = g.with(h).with(a);
        s.add(x,y,z,r,u,someProduct,someName,k);
        s.add(abs1,abs2,abs3,k.abs(b,p->true));
        System.out.println(z);
        System.out.println(z.symbol);
        System.out.println(z.disjuncts);
        System.out.println(s.verifyStates());
        s.renderStates();
    }
}
