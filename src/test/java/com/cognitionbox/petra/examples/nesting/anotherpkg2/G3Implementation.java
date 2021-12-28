package com.cognitionbox.petra.examples.nesting.anotherpkg2;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.examples.nesting.SomeView2;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class G3Implementation implements Consumer<SomeView2> {
    @Override
    public void accept(SomeView2 f) {
        kases(f,kase(someView2->someView2.x(), someView2->someView2.y(), someView2->{

                }));
//        kases(foo,kase(f->f.b(), f->f.c(), f->{
//            seq(f, x->x.set("c"));
//        }),
//                kase(f->f.b(), f->f.c(), f->{
//                    seq(f, x->x.set("c"));
//                }));
    }
}
