package com.cognitionbox.petra.modelling4;

import static com.cognitionbox.petra.modelling4.Modelling.*;

public class ModellingMain {
    public static void main(String[] args){
        SYS s = system("w");
        STATE_SPACE ss = s.statespace();
        TRANSITIONS t = s.transitions();
        // i think all states them selfs need to be abtractions, which the symbolic state("a") for e.g. being a single object, with arrow to itself
        STATE x = ss.xor("a","b","c"); // xor is abstraction of containing states to the name (a xor b)
        STATE y = ss.xor("d","e");
        STATE z = x.with(y); // is abstraction of the states of product to the name (x product y)
        STATE r = state("r");
        STATE u = z.with(r);
        // this means all states ie abstractions can be rendered as abstractions
        ABS abs3 = z.abs(ss.xor(state("i"),state("j")), p->p.symbol=="c" || p.symbol=="b", q->q.symbol=="a");
        ABS abs1 = x.abs(y, p->p.symbol=="a" || p.symbol=="b", q->q.symbol=="c");
        STATE someName = state("someName");
        STATE someProduct = z.with(someName);
        ABS abs2 = y.abs(someName, p->true); // single name abstraction

        STATE k = state("g").with(state("h"));
        ss.add(x,y,z,r,u,someProduct,someName,k);
        ss.add(abs1,abs2,abs3);
        System.out.println(z);
        System.out.println(z.symbol);
        System.out.println(z.disjuncts);
        System.out.println(ss.verify());
        ss.render();
    }
}
