package com.cognitionbox.petra.examples.nesting;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.lang.step.PEdge;
import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;


@View
public interface G4_ extends SomeView2 {
    @Edge
    static void accept(SomeView2 f) {
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
