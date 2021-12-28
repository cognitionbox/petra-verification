package com.cognitionbox.petra.examples.nesting.anotherpkg2;

import com.cognitionbox.petra.examples.nesting.SomeView2;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

public class G3 implements Consumer<SomeView2> {
    @Override
    public void accept(SomeView2 f) {
        kases(f,kase(someView2->someView2.x(), someView2->someView2.y(), someView2->{
            seq(someView2, new G3Implementation());
        }));
    }
}
