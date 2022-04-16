package com.cognitionbox.petra.examples.nesting;

import com.cognitionbox.petra.lang.step.PGraph;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

public interface G4 extends PGraph<SomeView2> {
    static void accept(SomeView2 f) {
        kases(f,kase(someView2->someView2.y(), someView2->someView2.z(), someView2->{
            seq(someView2, G4_::accept);
        }));
    }
}
