package com.cognitionbox.petra.examples.mainexample;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

/*
 * Idea will be to have a parser for Petra Java written in Maude that restricts the Java language to that of Petra Java
 * and allows Petra Java to be manipulated as one big term.
 */
public class A implements Consumer<Foo> {
    @Override
    public void accept(Foo foo) {
        kases(foo,kase(f->true, f->true, f->{
            seq(f, new A());
            seq(f, new A());
            seq(f, new A());
            seq(f, new A());
            join(
                    par(f, new A()),
                    par(f.fee1, new B()),
                    par(f.fee2, new B()),
                    par(f, new A())
            );
            seqr(f.list, new B());
        }),
        kase(f->true, f->true, f->{

        }),
        kase(f->true, f->true, f->{

        }));
    }
}
