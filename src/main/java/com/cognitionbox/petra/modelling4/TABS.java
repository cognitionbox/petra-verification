package com.cognitionbox.petra.modelling4;

public class TABS {
    private TRANS from;
    private TRAN too;
    public TABS(TRANS from, TRAN too) {
        this.from = from;
        this.too = too;
    }

    public TRANS getFrom() {
        return from;
    }

    public TRAN getToo() {
        return too;
    }
}
