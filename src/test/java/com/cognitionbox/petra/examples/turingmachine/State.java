package com.cognitionbox.petra.examples.turingmachine;

import com.cognitionbox.petra.lang.collection.PList;
import com.cognitionbox.petra.lang.primitives.PValue;
import com.cognitionbox.petra.lang.primitives.impls.PInteger;

import java.util.List;

public class State implements StateView {
    private final PValue<StateValue> state = new PValue();
    private final PInteger head = new PInteger();
    private final List<StateValue> tape = new PList<>();

    @Override
    public StateValue state() {
        return state.get();
    }

    @Override
    public StateValue headValue() {
        return tape.get(head.get());
    }
}
