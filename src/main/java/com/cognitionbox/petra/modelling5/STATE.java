package com.cognitionbox.petra.modelling5;

import com.google.common.collect.Sets;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.cognitionbox.petra.modelling5.MODELLING.state;

public class STATE {
    enum Source {
        WITH,
        WITHOUT
    }
    Source source = null;
    Set<STATE> productComponents;
    List<STATE> conjuncts;
    Set<STATE> disjuncts;
    String symbol;
    private STATE(){}
    STATE(String symbol, STATE... states){
        this.symbol = symbol;
        this.disjuncts = new HashSet<>(Arrays.asList(states));
    }
    STATE(String symbol, Set<STATE> states){
        this.symbol = symbol;
        this.disjuncts = states;
    }
    STATE(String symbol, Set<STATE> states, Set<STATE> productComponents, Source source){
        this.symbol = symbol;
        this.disjuncts = states;
        this.productComponents = productComponents;
        this.source = source;
    }
    STATE(String symbol, List<STATE> states){
        this.symbol = symbol;
        this.conjuncts = states;
    }
    STATE(String symbol){
        this.symbol = symbol;
        STATE s = new STATE();
        s.symbol = this.symbol;
        this.disjuncts = new HashSet<>(Arrays.asList(s));
    }
    // original one vs above
//    STATE(String symbol) {
//        this.symbol = symbol;
//    }
    public ABS abs(STATE s, Predicate<STATE>... via){
        return new ABS(this,s,via);
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

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        State state = (State) o;
//        return Objects.equals(conjuncts, state.conjuncts) || Objects.equals(disjuncts, state.disjuncts) || Objects.equals(symbol, state.symbol);
//    }
//
//    @Override
//    public int hashCode() {
//        if (conjuncts!=null && !conjuncts.isEmpty()){
//            return Objects.hash(conjuncts);
//        } else if (disjuncts!=null && !disjuncts.isEmpty()){
//            return Objects.hash(disjuncts);
//        } else if (symbol!=null){
//            return Objects.hash(symbol);
//        }
//        throw new IllegalStateException();
//    }


    public STATE with(STATE... states){
        StringBuilder conjunctionString = new StringBuilder();
        conjunctionString.append(this.symbol);
        for (int i=0;i<states.length;i++){
            conjunctionString.append(" & "+states[i].symbol);
        }
        // may or may not need this due to new state(name) impl which is about handling
        // atomic states.
//        if (this.disjuncts==null && states.length==1 && states[0].disjuncts==null){
//            return concat(this,states[0]);
//        }

        List<Set<STATE>> list = new ArrayList<>();
        list.add(this.disjuncts);
        Set<STATE> set = new HashSet<>();
        set.add(this);
        for (int i=0;i<states.length;i++){
            if (states[i].disjuncts!=null){
                list.add(states[i].disjuncts);
            } else if (states[i].conjuncts!=null){
                list.add(new HashSet<>(Arrays.asList(concat(states[i].conjuncts))));
            } else if (states[i].symbol!=null){
                list.add(new HashSet<>(Arrays.asList(state(states[i].symbol))));
            }
            set.add(states[i]);
        }
        return new STATE(conjunctionString.toString(),
                Sets.cartesianProduct(list).stream().map(c->concat(c)).collect(Collectors.toSet()),
                set,
                Source.WITH);
    }

    public STATE without(STATE state){
        StringBuilder conjunctionString = new StringBuilder();
        Set<STATE> productComponents = new HashSet<>(this.productComponents);
        productComponents.remove(state);
        Set<STATE> remaining = productComponents;
        STATE[] states = new STATE[remaining.size()];
        states = new ArrayList<>(remaining).toArray(states);
        for (int i=0;i<states.length;i++){
            if (i==0){
                conjunctionString.append(states[i].symbol);
            } else {
                conjunctionString.append(" & "+states[i].symbol);
            }
        }

        List<Set<STATE>> list = new ArrayList<>();
        for (int i=0;i<states.length;i++){
            if (states[i].disjuncts!=null){
                list.add(states[i].disjuncts);
            } else if (states[i].conjuncts!=null){
                list.add(new HashSet<>(Arrays.asList(concat(states[i].conjuncts))));
            } else if (states[i].symbol!=null){
                list.add(new HashSet<>(Arrays.asList(state(states[i].symbol))));
            }
        }
        return new STATE(conjunctionString.toString(),
                Sets.cartesianProduct(list).stream().map(c->concat(c)).collect(Collectors.toSet()),
                remaining,
                Source.WITHOUT);
    }

    public SYSTEM system(String symbol){
        return new SYSTEM(symbol);
    }

    private STATE concat(STATE... states){
        StringBuilder conjunctionString = new StringBuilder();
        conjunctionString.append(states[0].symbol);
        for (int i=1;i<states.length;i++){
            conjunctionString.append(" & "+states[i].symbol);
        }
        return new STATE(conjunctionString.toString(),Arrays.asList(states));
    }

    STATE concat(List<STATE> states){
        StringBuilder conjunctionString = new StringBuilder();
        conjunctionString.append(states.get(0).symbol);
        for (int i=1;i<states.size();i++){
            conjunctionString.append(" & "+states.get(i).symbol);
        }
        return new STATE(conjunctionString.toString(),states);
    }
}
