package com.cognitionbox.petra.examples.lightingsystemsimple2;

import com.cognitionbox.petra.lang.primitives.impls.PBoolean;

public class Power implements PowerView {
    private PBoolean active = new PBoolean();

    @Override
    public PBoolean active() {
        return active;
    }
}
