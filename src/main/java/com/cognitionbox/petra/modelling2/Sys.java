package com.cognitionbox.petra.modelling2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Sys {
    public Sys(String symbol) {
        this.symbol = symbol;
    }
    String symbol;
    Set<State> states = new HashSet<>();
    Set<Abstraction> stateAbsts = new HashSet<>();
    Set<Transition> transitions = new HashSet<>();

    public void render(){
        StringBuilder sb = new StringBuilder();
        for (State s : states){

        }
    }
    public void render(State state, StringBuilder sb){
        if (state.symbol!=null){
            sb.append(state.symbol+";");
        } else if (state.disjuncts!=null && !state.disjuncts.isEmpty()){
            for (State d : state.disjuncts){
                //render(d,sb);
            }
        } else if (state.conjuncts!=null && !state.conjuncts.isEmpty()){
            for (State d : state.disjuncts){
                //render(d,sb);
            }
        }
        for (State s : states){
            if (s.symbol!=null){
                sb.append(s.symbol+";");
            }
        }
    }

    public boolean verify(){
        return checkStateAbstractSoundess();
    }

    private boolean checkStateAbstractSoundess(){
        return stateAbsts.stream().allMatch(a->a.isSound());
    }

    public void add(State... s){
//        for (State e : s){
//            if (this.states.stream().anyMatch(i->i.equals(e))){
//                throw new IllegalStateException("state not unique");
//            }
//        }
        this.states.addAll(Arrays.asList(s));
    }
    public void add(State s){
        this.states.add(s);
    }
    public void add(Transition s){
        this.transitions.add(s);
    }
    public void add(Abstraction s){
        this.stateAbsts.add(s);
        add(s.from());
        add(s.too());
    }

    public State state(String symbol){
        State s = new State(symbol);
        return s;
    }
    public State xor(State... states){
        State s = new State(states);
        return s;
    }
    public State and(State... states){
        State s = new State(Arrays.asList(states));
        return s;
    }
    public State and(List<State> states){
        State s = new State(states);
        return s;
    }
    public Sys system(String symbol){
        return new Sys(symbol);
    }
}
