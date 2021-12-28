package com.cognitionbox.petra.modelling4;

import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

public final class Modelling {
    public static STATE state(String symbol){
        return new STATE(symbol);
    }
    public static TRAN tran(STATE a, STATE b){
        return new TRAN(a,b);
    }
    public static TRANS trans(TRAN... s){
        return new TRANS(Arrays.asList(s));
    }
    public static SYS system(String symbol){
        return new SYS(symbol);
    }

}
