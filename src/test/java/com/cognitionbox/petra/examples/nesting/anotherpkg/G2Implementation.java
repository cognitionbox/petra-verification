package com.cognitionbox.petra.examples.nesting.anotherpkg;

import com.cognitionbox.petra.lang.step.PEdge;
import com.cognitionbox.petra.examples.nesting.SomeView2;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;


public class G2Implementation implements PEdge<SomeView2> {
    @Override
    public void accept(SomeView2 f) {
        kases(f,kase(someView2->someView2.w(), someView2->someView2.x(), someView2->{

        }));
    }
}
