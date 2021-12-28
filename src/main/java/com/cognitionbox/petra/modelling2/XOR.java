package com.cognitionbox.petra.modelling2;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class XOR implements Abstraction{
    final State from;
    final State too;
    private Predicate<State>[] via;
    public XOR(State from, State too, Predicate<State>... via) {
        this.from = from;
        this.too = too;
        this.via = via;
    }

    public boolean isSound(){
        return this.from.symbol!=null && this.too.symbol!=null ||
                this.from.disjuncts!=null && !this.from.disjuncts.isEmpty() && this.too.symbol!=null ||
                this.from.disjuncts!=null && !this.from.disjuncts.isEmpty() && !this.too.disjuncts.isEmpty() && isSurjection() ||
                this.from.conjuncts!=null && !this.from.conjuncts.isEmpty() && this.too.symbol!=null;
    }

    private boolean isSurjection(){
        Set<State> mapped = new HashSet<>();
        Set<Set<State>> collected = new HashSet<>();
        for (Predicate<State> p : via){
            Set x = this.from.disjuncts.stream().filter(p).collect(Collectors.toSet());
            collected.add(x);
            if (!x.isEmpty()){
                mapped.addAll(x);
            }
        }
        for (Set<State> a : collected){
            for (Set<State> b : collected){
                if (!a.equals(b)){
                    Set c = new HashSet<>(a);
                    c.retainAll(b);
                    if (!c.isEmpty()){
                        return false;
                    }
                }
            }
        }
        return mapped.equals(from.disjuncts);
    }

    @Override
    public State from() {
        return from;
    }

    @Override
    public State too() {
        return too;
    }
}
