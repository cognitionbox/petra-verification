package com.cognitionbox.petra.examples.nesting.anotherpkg;

import com.cognitionbox.petra.annotations.Graph;
import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.examples.nesting.SomeView2;
import com.cognitionbox.petra.lang.step.PGraph;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

@View public interface G2 extends SomeView2 {
    @Graph static void accept(SomeView2 f) {
        kases(f,kase(someView2->someView2.w(), someView2->someView2.x(), someView2->{
            seq(someView2, G2Implementation::accept);
        }));
//        kases(foo,kase(f->f.a(), f->f.b(), f->{
//            seq(f, new G2_());
//        }));
    }
}
