package com.cognitionbox.petra.examples.lightingsystemsimple;

import com.cognitionbox.petra.lang.primitives.impls.PBoolean;

public class Switch implements SwitchView{
    private PBoolean active = new PBoolean();

    @Override
    public PBoolean active() {
        return active;
    }
}
