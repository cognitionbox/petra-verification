package com.cognitionbox.petra.modelling3;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ABS {
    public STATE getFrom() {
        return from;
    }

    public STATE getToo() {
        return too;
    }

    private STATE from;
    private STATE too;
    private Predicate<STATE>[] via;
    public ABS(STATE from, STATE too, Predicate<STATE>... via) {
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
        Set<STATE> mapped = new HashSet<>();
        Set<Set<STATE>> collected = new HashSet<>();
        for (Predicate<STATE> p : via){
            Set x = this.from.disjuncts.stream().filter(p).collect(Collectors.toSet());
            collected.add(x);
            if (!x.isEmpty()){
                mapped.addAll(x);
            }
        }
        for (Set<STATE> a : collected){
            for (Set<STATE> b : collected){
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
}
