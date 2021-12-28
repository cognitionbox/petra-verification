package com.cognitionbox.petra.modelling;

import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public final class Modelling {
    public static State state(String symbol){
        return new State(symbol);
    }
    public static State xor(State... states){
        return new State(states);
    }
    public static State and(State... states){
        return new State(Arrays.asList(states));
    }
    public static State and(List<State> states){
        return new State(states);
    }
    public static Sys system(String symbol){
        return new Sys(symbol);
    }
    public static State product(State... states){
        return new State(Sets.cartesianProduct(new HashSet<>(Arrays.asList(states))).stream().map(c->and(c)).collect(Collectors.toSet()));
    }
}
