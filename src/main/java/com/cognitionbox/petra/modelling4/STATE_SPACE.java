package com.cognitionbox.petra.modelling4;

import java.util.*;
import java.util.stream.Collectors;

import static com.cognitionbox.petra.modelling4.Modelling.state;

public class STATE_SPACE {
    private Map<String, STATE> statesMap = new HashMap<>();
    public void add(STATE... s){
        for (STATE e : s){
            if (e.disjuncts!=null){
                addImpl(e);
                for (STATE i : e.disjuncts){
                    add(i);
                }
            }
        }
    }
    private void addImpl(STATE e){
        if (!this.states.contains(e)){
            this.states.add(e);
            this.statesMap.put(e.symbol,e);
        } else {
            //throw new IllegalStateException();
        }
    }
    public STATE state(String name){
        return this.statesMap.get(name);
    }
    public void add(ABS... s){
        this.stateAbsts.addAll(Arrays.asList(s));
    }
    public void add(ABS s){
        this.stateAbsts.add(s);
    }

    public STATE xor(String... states){
        StringBuilder disjunctionString = new StringBuilder();
        disjunctionString.append(states[0]);
        for (int i=1;i<states.length;i++){
            disjunctionString.append(" + "+states[i]);
        }
        return new STATE(disjunctionString.toString(), Arrays.asList(states).stream().map(s->new STATE(s)).collect(Collectors.toSet()));
    }
    public STATE xor(STATE... states){
        StringBuilder disjunctionString = new StringBuilder();
        disjunctionString.append(states[0].symbol);
        for (int i=1;i<states.length;i++){
            disjunctionString.append(" + "+states[i].symbol);
        }
        return new STATE(disjunctionString.toString(), states);
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

    List<STATE> states = new ArrayList<>();
    List<ABS> stateAbsts = new ArrayList<>();

    public void render(){
        StringBuilder sb = new StringBuilder();
        sb.append("digraph {\n");
        sb.append("rankdir=LR;\n");
        sb.append("splines=line;\n");
        for (STATE s : states){
            sb.append("\""+s.symbol+"\""+";\n");
            if (s.disjuncts!=null){
                for (STATE e : s.disjuncts){
                    sb.append("\""+e.symbol+"\""+";\n");
                    sb.append("\""+s.symbol+"\"->\""+e.symbol+"\""+" [label=\"+\"];\n");
                }
            }
            if (s.conjuncts!=null){
                for (STATE e : s.conjuncts){
                    sb.append("\""+e.symbol+"\""+";\n");
                    sb.append("\""+s.symbol+"\"->\""+e.symbol+"\""+" [label=\"&\"];\n");
                }
            }
        }
        for (ABS abs : stateAbsts){
            for (STATE i : abs.getFrom().disjuncts){
                if (abs.getToo().disjuncts!=null){
                    for (STATE j : abs.getToo().disjuncts){
                        sb.append("\""+i.symbol+"\"->\""+j.symbol+"\""+" [label=\"\"];\n");
                    }
                } else if (abs.getToo().conjuncts!=null){
                    for (STATE j : abs.getToo().conjuncts){
                        sb.append("\""+i.symbol+"\"->\""+concat(j.conjuncts).symbol+"\""+" [label=\"\"];\n");
                    }
                } else if (abs.getToo().symbol!=null){
                    sb.append("\""+i.symbol+"\"->\""+abs.getToo().symbol+"\""+" [label=\"\"];\n");
                }
            }
        }
        for (STATE i : states) {
            for (STATE j : states) {
                if (j.symbol.contains(i.symbol) && !j.symbol.equals(i.symbol)){
                    sb.append("\"" + i.symbol + "\"->\"" + j.symbol + "\"" + " [label=\"&\"];\n");
                }
            }
        }
        sb.append("}");
        System.out.println(sb.toString());
    }

    public boolean verify(){
        return checkStateAbstractSoundess();
    }

    private boolean checkStateAbstractSoundess(){
        return stateAbsts.stream().allMatch(a->a.isSound());
    }
}
