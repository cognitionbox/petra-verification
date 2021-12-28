package com.cognitionbox.petra.modelling5;

import java.util.ArrayList;
import java.util.List;

public final class TRANS implements TRANSITION {
    List<TRAN> sequentialTransitions = new ArrayList<>();
    public TRANS(List<TRAN> sequentialTransitions) {
        this.sequentialTransitions = sequentialTransitions;
    }

    @Override
    public STATE getFrom() {
        return sequentialTransitions.get(0).getFrom();
    }

    @Override
    public STATE getToo() {
        return sequentialTransitions.get(sequentialTransitions.size()-1).getToo();
    }

    public TABS abs(TRAN transition){
        return new TABS(this,transition);
    }
}
