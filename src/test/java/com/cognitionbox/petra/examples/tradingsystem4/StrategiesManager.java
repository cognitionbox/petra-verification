package com.cognitionbox.petra.examples.tradingsystem4;

import com.cognitionbox.petra.examples.tradingsystem4.decision.Strategy;
import com.cognitionbox.petra.examples.tradingsystem4.decision.views.StrategiesEffectedView;
import com.cognitionbox.petra.examples.tradingsystem4.decision.views.StrategiesPreparedView;
import com.cognitionbox.petra.lang.collection.PCollection;
import com.cognitionbox.petra.lang.collection.PList;

public class StrategiesManager implements StrategiesPreparedView, StrategiesEffectedView {
    private final PCollection<Strategy> strategies = new PList<>();
    public PCollection<Strategy> strategies() {
        return strategies;
    }
}
