package com.cognitionbox.petra.modelling5;

import java.util.*;
import java.util.stream.Collectors;

public class STATE_SPACE {
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
        } else {
            //throw new IllegalStateException();
        }
    }
    public void add(ABS... s){
        this.stateAbsts.addAll(Arrays.asList(s));
    }
    public void add(ABS s){
        this.stateAbsts.add(s);
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
    Set<String> appended = new HashSet<>();
    void appendIfNotAlreadyAppended(StringBuilder sb, String toAppend){
        if (isIdentityArrow(toAppend)){
            return;
        }
        if (!appended.contains(toAppend)){
            sb.append(toAppend);
            appended.add(toAppend);
        }
    }
    private boolean isIdentityArrow(String dotCommand){
        if (dotCommand.split("->").length==2){
            String[] split1 = dotCommand.split("\\[");
            String[] split2 = split1[0].replaceAll(" ","").split("->");
            if (split2[0].equals(split2[1])){
                return true;
            }
        }
        return false;
    }
    public void render(){
        StringBuilder sb = new StringBuilder();
        sb.append("digraph {\n");
        sb.append("rankdir=LR;\n");
        sb.append("splines=line;\n");
        for (STATE s : states){
            appendIfNotAlreadyAppended(sb,"\""+s.symbol+"\""+";\n");
            if (s.disjuncts!=null){
                for (STATE e : s.disjuncts){
                    appendIfNotAlreadyAppended(sb,"\""+e.symbol+"\""+";\n");
                    appendIfNotAlreadyAppended(sb,"\""+s.symbol+"\"->\""+e.symbol+"\""+" [xlabel=\"+\"];\n");
                }
            }
            if (s.conjuncts!=null){
                for (STATE e : s.conjuncts){
                    appendIfNotAlreadyAppended(sb,"\""+e.symbol+"\""+";\n");
                    appendIfNotAlreadyAppended(sb,"\""+s.symbol+"\"->\""+e.symbol+"\""+" [xlabel=\"&\"];\n");
                }
            }
        }
        for (ABS abs : stateAbsts){
            for (STATE i : abs.getFrom().disjuncts){
                if (abs.getToo().disjuncts!=null){
                    for (STATE j : abs.getToo().disjuncts){
                        appendIfNotAlreadyAppended(sb,"\""+i.symbol+"\"->\""+j.symbol+"\""+" [xlabel=\"\"];\n");
                    }
                } else if (abs.getToo().conjuncts!=null){
                    for (STATE j : abs.getToo().conjuncts){
                        appendIfNotAlreadyAppended(sb,"\""+i.symbol+"\"->\""+concat(j.conjuncts).symbol+"\""+" [xlabel=\"\"];\n");
                    }
                } else if (abs.getToo().symbol!=null){
                    appendIfNotAlreadyAppended(sb,"\""+i.symbol+"\"->\""+abs.getToo().symbol+"\""+" [xlabel=\"\"];\n");
                }
            }
        }
        for (STATE i : states) {
            for (STATE j : states) {
                if (isConjunctOf(j.symbol,i.symbol) && !j.symbol.equals(i.symbol)){
                    appendIfNotAlreadyAppended(sb,"\"" + i.symbol + "\"->\"" + j.symbol + "\"" + " [xlabel=\"&\"];\n");
                }
            }
        }
        appendIfNotAlreadyAppended(sb,"}");
        System.out.println(sb.toString());
    }

    private boolean isConjunctOf(String conjunction, String state){
        String[] conjuncts = conjunction.split("&");
        for (String c : conjuncts){
            if (c.replaceAll(" ","").equals(state.replaceAll(" ",""))){
                return true;
            }
        }
        return false;
    }

    public boolean verify(){
        return checkStateAbstractSoundess();
    }

    private boolean checkStateAbstractSoundess(){
        return stateAbsts.stream().allMatch(a->a.isSound());
    }
}
