package com.cognitionbox.petra.examples.lightingsystem;

public class Power {
    private boolean active;
    void powerOn(){
        active = true;
    }
    void powerOff(){
        active = false;
    }
    boolean on(){
        return active;
    }

    public boolean off() {
        return !active;
    }
}
