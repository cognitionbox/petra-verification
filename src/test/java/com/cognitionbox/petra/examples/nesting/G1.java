package com.cognitionbox.petra.examples.nesting;

import com.cognitionbox.petra.examples.nesting.anotherpkg.G2;
import com.cognitionbox.petra.examples.nesting.anotherpkg2.G3;
import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.*;

public class G1 implements PGraph<SomeView2> {
    @Override
    public void accept(SomeView2 f) {
        kases(f,kase(someView2->someView2.w(), someView2->someView2.z(), someView2->{
            seq(someView2, new G2());
            seq(someView2, new G3());
            seq(someView2, new G4());
            //join(par(f,new G3()), par(f,new G4()));
        }));
    }
}
