package com.cognitionbox.petra.modelling5;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class MODELLING {
    public static STATE state(String symbol){
        return new STATE(symbol);
    }
    public static TRAN tran(STATE a, STATE b){
        return new TRAN(a,b);
    }
    public static TRANS trans(TRAN... s){
        return new TRANS(Arrays.asList(s));
    }
    public static SYSTEM system(String symbol){
        return new SYSTEM(symbol);
    }

    public static STATE xor(String... states){
        StringBuilder disjunctionString = new StringBuilder();
        disjunctionString.append(states[0]);
        for (int i=1;i<states.length;i++){
            disjunctionString.append(" + "+states[i]);
        }
        return new STATE(disjunctionString.toString(), Arrays.asList(states).stream().map(s->new STATE(s)).collect(Collectors.toSet()));
    }

    public static STATE xor(STATE... states){
        StringBuilder disjunctionString = new StringBuilder();
        disjunctionString.append(states[0].symbol);
        for (int i=1;i<states.length;i++){
            disjunctionString.append(" + "+states[i].symbol);
        }
        return new STATE(disjunctionString.toString(), states);
    }

}
