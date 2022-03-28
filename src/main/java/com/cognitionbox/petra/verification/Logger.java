package com.cognitionbox.petra.verification;

import static com.cognitionbox.petra.verification.Strings.NEW_LINE;

public class Logger {
    private final boolean debugEnabled = false;
    public void info(String s) {
        System.out.println("INFO: "+s+NEW_LINE);
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
