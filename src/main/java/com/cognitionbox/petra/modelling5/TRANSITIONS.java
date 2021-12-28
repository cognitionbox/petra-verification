package com.cognitionbox.petra.modelling5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TRANSITIONS {
    STATE_SPACE stateSpace;
    public TRANSITIONS(STATE_SPACE stateSpace) {
        this.stateSpace = stateSpace;
    }

    List<TRANSITION> transitionList = new ArrayList<>();
    List<TABS> transitionAbstractionList = new ArrayList<>();
    public void add(TRAN t){
        this.transitionList.add(t);
    }
    public void add(TRANS ts){
        this.transitionList.add(ts);
    }
    public void render(){
        StringBuilder sb = new StringBuilder();
        sb.append("digraph {\n");
        sb.append("rankdir=LR;\n");
        sb.append("splines=line;\n");
        for (TRANSITION t : transitionList){
            sb.append("\""+t.getFrom().symbol+"\"->\""+t.getToo().symbol+"\""+" [label=\"t\"];\n");
            if (t instanceof TRANS){
                for (TRAN i : ((TRANS) t).sequentialTransitions){
                    sb.append("\""+i.getFrom().symbol+"\"->\""+i.getToo().symbol+"\""+" [label=\"t\"];\n");
                }
            }
//            for (ABS abs : stateSpace.stateAbsts){
//                for (STATE s : abs.getFrom().disjuncts){
//                    if (t.getFrom().){
//
//                    }
//                }
//            }
        }
        for (TABS abs : transitionAbstractionList){
            sb.append("\""+abs.getFrom().getFrom().symbol+"\"->\""+abs.getToo().getFrom().symbol+"\""+" [label=\"\"];\n");
            sb.append("\""+abs.getFrom().getToo().symbol+"\"->\""+abs.getToo().getToo().symbol+"\""+" [label=\"\"];\n");
        }
        sb.append("}");
        System.out.println(sb.toString());
    }

    public void add(TABS abs) {
        this.transitionAbstractionList.add(abs);
    }

    boolean verifyTransitions(){
        boolean ok = true;
        for (TABS abs : transitionAbstractionList){
            ok = ok && abs.getFrom().getFrom().symbol.equals(abs.getToo().getFrom().symbol);
            ok = ok && abs.getFrom().getToo().symbol.equals(abs.getToo().getToo().symbol);
        }
        return ok;
    }
}
