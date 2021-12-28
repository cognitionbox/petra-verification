package com.cognitionbox.petra.modelling2;

import java.util.*;
import java.util.function.Predicate;

public final class State {
    List<State> conjuncts;
    Set<State> disjuncts;
    String symbol;
    State(State... states){
        disjuncts = new HashSet<>(Arrays.asList(states));
    }
    State(Set<State> states){
        disjuncts = states;
    }
    State(List<State> states){
        conjuncts = states;
    }
    State(String symbol){
        this.symbol = symbol;
    }
    public XOR abs(State s, Predicate<State>... via){
        return new XOR(this,s,via);
    }

    @Override
    public String toString() {
        if (conjuncts!=null && !conjuncts.isEmpty()){
            return conjuncts.toString();
        } else if (disjuncts!=null && !disjuncts.isEmpty()){
            return disjuncts.toString();
        } else if (symbol!=null && !symbol.isEmpty()){
            return symbol.toString();
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return Objects.equals(conjuncts, state.conjuncts) || Objects.equals(disjuncts, state.disjuncts) || Objects.equals(symbol, state.symbol);
    }

    @Override
    public int hashCode() {
        if (conjuncts!=null && !conjuncts.isEmpty()){
            return Objects.hash(conjuncts);
        } else if (disjuncts!=null && !disjuncts.isEmpty()){
            return Objects.hash(disjuncts);
        } else if (symbol!=null){
            return Objects.hash(symbol);
        }
        throw new IllegalStateException();
    }
}
