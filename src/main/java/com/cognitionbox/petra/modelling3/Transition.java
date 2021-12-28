package com.cognitionbox.petra.modelling3;

public final class Transition {
    private STATE from;
    private STATE too;

    public Transition(STATE from, STATE too) {
        this.from = from;
        this.too = too;
    }
}
