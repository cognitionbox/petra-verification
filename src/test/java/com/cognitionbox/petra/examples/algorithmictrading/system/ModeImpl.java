package com.cognitionbox.petra.examples.algorithmictrading.system;

import com.cognitionbox.petra.lang.primitives.impls.PBoolean;

public class ModeImpl implements Mode {
    final private PBoolean mode = new PBoolean();
    @Override
    public PBoolean mode() {
        return mode;
    }
}
