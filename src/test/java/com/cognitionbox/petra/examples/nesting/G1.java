package com.cognitionbox.petra.examples.nesting;

import com.cognitionbox.petra.annotations.Graph;
import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.examples.nesting.anotherpkg.G2;
import com.cognitionbox.petra.examples.nesting.anotherpkg2.G3;
import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.*;

@View public interface G1 extends SomeView2 {
    @Graph static void accept(SomeView2 f) {
        kases(f,kase(someView2->someView2.w(), someView2->someView2.z(), someView2->{
            seq(someView2, G2::accept);
            seq(someView2, G3::accept);
            seq(someView2, G4::accept);
            //join(par(f,new G3()), par(f,new G4()));
        }));
    }
}
