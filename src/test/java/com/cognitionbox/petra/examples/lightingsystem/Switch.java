package com.cognitionbox.petra.examples.lightingsystem;

public class Switch {
    private boolean active;
    void switchOn(){
        active = true;
    }
    void switchOff(){
        active = false;
    }
    boolean off(){
        return !active;
    }

    public boolean on() {
        return active;
    }
}
