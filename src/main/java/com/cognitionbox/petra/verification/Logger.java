package com.cognitionbox.petra.verification;

public class Logger {
    private final boolean debugEnabled = false;
    public void info(String s) {
        System.out.println("INFO: "+s+"\n");
    }

    public void debug(String s) {
        if (debugEnabled){
            System.out.println("DEBUG: "+s);
        }
    }

    public void error(String s) {
        System.out.println("ERROR: "+s);
    }
}
