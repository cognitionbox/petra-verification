package com.cognitionbox.petra.examples.nesting;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

public class G4 implements Consumer<SomeView2> {
    @Override
    public void accept(SomeView2 f) {
        kases(f,kase(someView2->someView2.y(), someView2->someView2.z(), someView2->{
            seq(someView2, new G4_());
        }));
    }
}
