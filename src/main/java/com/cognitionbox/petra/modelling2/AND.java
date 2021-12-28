package com.cognitionbox.petra.modelling2;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AND implements Abstraction{
    final State from;
    final State too;
    public AND(State from, State too) {
        this.from = from;
        this.too = too;
    }

    @Override
    public State from() {
        return from;
    }

    @Override
    public State too() {
        return too;
    }

    @Override
    public boolean isSound() {
        return true;
    }
}
