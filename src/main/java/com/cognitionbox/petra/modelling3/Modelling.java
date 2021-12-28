package com.cognitionbox.petra.modelling3;

import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

public final class Modelling {
    public static STATE state(String symbol){
        return new STATE(symbol);
    }
    public static STATE xor(String... states){
        StringBuilder disjunctionString = new StringBuilder();
        disjunctionString.append(states[0]);
        for (int i=1;i<states.length;i++){
            disjunctionString.append(" + "+states[i]);
        }
        return new STATE(disjunctionString.toString(), Arrays.asList(states).stream().map(s->state(s)).collect(Collectors.toSet()));
    }
    public static STATE xor(STATE... states){
        StringBuilder disjunctionString = new StringBuilder();
        disjunctionString.append(states[0].symbol);
        for (int i=1;i<states.length;i++){
            disjunctionString.append(" + "+states[i].symbol);
        }
        return new STATE(disjunctionString.toString(), states);
    }
    public static STATE and(STATE... states){
        StringBuilder conjunctionString = new StringBuilder();
        conjunctionString.append(states[0].symbol);
        for (int i=1;i<states.length;i++){
            conjunctionString.append(" & "+states[i].symbol);
        }
        return new STATE(conjunctionString.toString(),Arrays.asList(states));
    }
    public static STATE and(List<STATE> states){
        StringBuilder conjunctionString = new StringBuilder();
        conjunctionString.append(states.get(0).symbol);
        for (int i=1;i<states.size();i++){
            conjunctionString.append(" & "+states.get(i).symbol);
        }
        return new STATE(conjunctionString.toString(),states);
    }
    public static SYS system(String symbol){
        return new SYS(symbol);
    }
//    public static State product(State... states){
//        return new State(Sets.cartesianProduct(new HashSet<>(Arrays.asList(states))).stream().map(c->and(c)).collect(Collectors.toSet()));
//    }
    public static STATE product(STATE... states){
        StringBuilder conjunctionString = new StringBuilder();
        conjunctionString.append(states[0].symbol);
        for (int i=1;i<states.length;i++){
            conjunctionString.append(" & "+states[i].symbol);
        }

        List<Set<STATE>> list = new ArrayList<>();
        for (int i=0;i<states.length;i++){
            if (states[i].disjuncts!=null){
                list.add(states[i].disjuncts);
            } else if (states[i].conjuncts!=null){
                list.add(new HashSet<>(Arrays.asList(and(states[i].conjuncts))));
            } else if (states[i].symbol!=null){
                list.add(new HashSet<>(Arrays.asList(state(states[i].symbol))));
            }
        }
        return new STATE(conjunctionString.toString(), Sets.cartesianProduct(list).stream().map(c->and(c)).collect(Collectors.toSet()));
    }
}
