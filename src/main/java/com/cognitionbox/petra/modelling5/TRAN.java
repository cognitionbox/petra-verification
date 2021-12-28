package com.cognitionbox.petra.modelling5;

public final class TRAN implements TRANSITION {
    private STATE from;
    private STATE too;

    public STATE getFrom() {
        return from;
    }

    public STATE getToo() {
        return too;
    }

    public TRAN(STATE from, STATE too) {
        this.from = from;
        this.too = too;
    }
}
