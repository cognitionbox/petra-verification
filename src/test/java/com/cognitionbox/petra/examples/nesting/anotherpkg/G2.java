package com.cognitionbox.petra.examples.nesting.anotherpkg;

import com.cognitionbox.petra.examples.nesting.SomeView2;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

public class G2 implements Consumer<SomeView2> {
    @Override
    public void accept(SomeView2 f) {
        kases(f,kase(someView2->someView2.w(), someView2->someView2.x(), someView2->{
            seq(someView2, new G2Implementation());
        }));
//        kases(foo,kase(f->f.a(), f->f.b(), f->{
//            seq(f, new G2_());
//        }));
    }
}
