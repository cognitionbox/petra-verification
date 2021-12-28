package com.cognitionbox.petra.modelling;

public final class Transition {
    private State from;
    private State too;

    public Transition(State from, State too) {
        this.from = from;
        this.too = too;
    }
}
