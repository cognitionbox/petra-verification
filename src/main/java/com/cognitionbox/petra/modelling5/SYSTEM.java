package com.cognitionbox.petra.modelling5;

public class SYSTEM {
    private STATE_SPACE stateSpace = new STATE_SPACE();
    private TRANSITIONS transitions = new TRANSITIONS(stateSpace);
    public SYSTEM(String symbol) {
        this.symbol = symbol;
    }
    String symbol;

    public STATE_SPACE statespace() {
        return stateSpace;
    }

    public TRANSITIONS transitions() {
        return transitions;
    }

    public void add(STATE... s) {
        stateSpace.add(s);
    }

    public void add(ABS... s) {
        stateSpace.add(s);
    }

    public void add(ABS s) {
        stateSpace.add(s);
    }

    public void renderTransitions() {
        transitions.render();
    }

    public void renderStates() {
        stateSpace.render();
    }

    public boolean verifyStates() {
        return stateSpace.verify();
    }

    public void add(TRAN t) {
        transitions.add(t);
    }

    public void add(TRANS ts) {
        transitions.add(ts);
    }

    public void add(TABS abs) {
        transitions.add(abs);
    }

    public boolean verifyTransitions() {
        return transitions.verifyTransitions();
    }
}
