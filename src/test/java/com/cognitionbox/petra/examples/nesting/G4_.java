package com.cognitionbox.petra.examples.nesting;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class G4_ implements Consumer<SomeView2> {
    @Override
    public void accept(SomeView2 f) {
        kases(f,
        kase(someView2->someView2.y(), someView2->someView2.z(), someView2->{

        })
//        ,
//        kase(foo->foo.c(), foo->foo.d(), foo->{
//            seq(foo, x->x.set("d"));
//        })
        );
    }
}
