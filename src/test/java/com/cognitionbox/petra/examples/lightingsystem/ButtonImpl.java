package com.cognitionbox.petra.examples.lightingsystem;

import com.cognitionbox.petra.lang.primitives.impls.PBoolean;

public class ButtonImpl implements Button {
    private PBoolean active = new PBoolean();

    @Override
    public PBoolean active() {
        return active;
    }
}
