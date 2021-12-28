package com.cognitionbox.petra.examples.tradingsystem4;

import com.cognitionbox.petra.lang.collection.PCollection;
import com.cognitionbox.petra.lang.collection.PList;

public class DecisionsManager {
    private final PCollection<Decision> decisions = new PList<>();
    public PCollection<Decision> decisions() {
        return decisions;
    }
}
