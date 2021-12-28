package com.cognitionbox.petra.modelling4;

import java.util.HashMap;
import java.util.Map;

public class SYS {
    private STATE_SPACE stateSpace = new STATE_SPACE();
    private TRANSITIONS transitions = new TRANSITIONS();
    public SYS(String symbol) {
        this.symbol = symbol;
    }
    String symbol;

    public STATE_SPACE statespace() {
        return stateSpace;
    }

    public TRANSITIONS transitions() {
        return transitions;
    }
}
