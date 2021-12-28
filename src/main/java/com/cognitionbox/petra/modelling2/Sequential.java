package com.cognitionbox.petra.modelling2;

import java.util.Arrays;
import java.util.List;

public final class Sequential {
    private List<Transition> transitions;
    public Sequential(Transition... transitions) {
        this.transitions = Arrays.asList(transitions);
    }
}
