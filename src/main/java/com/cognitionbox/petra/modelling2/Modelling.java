package com.cognitionbox.petra.modelling2;

import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

import static com.cognitionbox.petra.modelling.Modelling.and;

public final class Modelling {
    public static AND state(String symbol){
        State s = new State(symbol);
        return new AND(s,s); // could have been XOR(s,s) too
    }
    public static XOR xor(Abstraction... states){
        StringBuilder disjunctionString = new StringBuilder();
        disjunctionString.append(states[0].too().symbol);
        for (int i=1;i<states.length;i++){
            disjunctionString.append(" + "+states[i].too().symbol);
        }
        AND disjunction = state(disjunctionString.toString());
        return new XOR(new State(Arrays.asList(states).stream().map(a->a.too()).collect(Collectors.toSet())),disjunction.too());
    }
    public static Sys system(String symbol){
        return new Sys(symbol);
    }
//    public static State and(State... states){
//        return new State(Sets.cartesianProduct(Arrays.asList(states).stream().map(s->s.disjuncts).collect(Collectors.toSet())).stream().map(c->concat(c)).collect(Collectors.toSet())));
//    }
    private static State andPrivate(List<State> states){
        List<Set<State>> list = new ArrayList<>();
        for (int i=0;i<states.size();i++){
            list.add(new HashSet<>(Arrays.asList(new State(states.get(i).symbol))));
        }
        return new State(Sets.cartesianProduct(list).stream().map(c->concat(c)).collect(Collectors.toSet()));
    }

    public static AND and(Abstraction... abstractions){
        for (Abstraction a : abstractions){
            if (a.too().symbol==null){
                throw new IllegalStateException("range is not a symbol");
            }
        }
        StringBuilder conjunctionString = new StringBuilder();
        conjunctionString.append(abstractions[0].too().symbol);
        for (int i=1;i<abstractions.length;i++){
            conjunctionString.append(" & "+abstractions[i].too().symbol);
        }
        AND conjunction = state(conjunctionString.toString());
        return new AND(andPrivate(Arrays.asList(abstractions).stream().map(a->a.too()).collect(Collectors.toList())),conjunction.too());
    }


//    private static State concat(State... states){
//        return new State(Arrays.asList(states));
//    }
    private static State concat(List<State> states){
        return new State(states);
    }
}
