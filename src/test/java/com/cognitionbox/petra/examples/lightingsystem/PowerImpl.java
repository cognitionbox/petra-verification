package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.lang.primitives.impls.PBoolean;

public class PowerImpl implements Power {
    private PBoolean active = new PBoolean();

    @Override
    public PBoolean active() {
        return active;
    }
}
