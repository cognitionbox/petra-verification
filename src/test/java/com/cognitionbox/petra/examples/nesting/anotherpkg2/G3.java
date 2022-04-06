package com.cognitionbox.petra.examples.nesting.anotherpkg2;

import com.cognitionbox.petra.examples.nesting.SomeView2;
import com.cognitionbox.petra.lang.step.PGraph;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

public interface G3 extends PGraph<SomeView2> {
    static void accept(SomeView2 f) {
        kases(f,kase(someView2->someView2.x(), someView2->someView2.y(), someView2->{
            seq(someView2, G3Implementation::accept);
        }));
    }
}
