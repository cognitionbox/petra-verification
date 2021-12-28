package com.cognitionbox.petra.modelling3;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.cognitionbox.petra.modelling3.Modelling.and;

public class SYS {
    public SYS(String symbol) {
        this.symbol = symbol;
    }
    String symbol;
    Set<STATE> states = new HashSet<>();
    Set<ABS> stateAbsts = new HashSet<>();
    Set<Transition> transitions = new HashSet<>();

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
                        sb.append("\""+i.symbol+"\"->\""+and(j.conjuncts).symbol+"\""+" [label=\"\"];\n");
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
    public void render(STATE state, StringBuilder sb){
        if (state.symbol!=null){
            sb.append(state.symbol+";");
        } else if (state.disjuncts!=null && !state.disjuncts.isEmpty()){
            for (STATE d : state.disjuncts){
                //render(d,sb);
            }
        } else if (state.conjuncts!=null && !state.conjuncts.isEmpty()){
            for (STATE d : state.disjuncts){
                //render(d,sb);
            }
        }
        for (STATE s : states){
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

    public void add(STATE... s){
        this.states.addAll(Arrays.asList(s));
    }
    public void add(ABS... s){
        this.stateAbsts.addAll(Arrays.asList(s));
    }
    public void add(STATE s){
        this.states.add(s);
    }
    public void add(Transition s){
        this.transitions.add(s);
    }
    public void add(ABS s){
        this.stateAbsts.add(s);
    }
    public SYS system(String symbol){
        return new SYS(symbol);
    }
}
