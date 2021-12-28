package com.cognitionbox.petra.examples.tradingsystem4.strat;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.examples.tradingsystem4.decision.Strategy;
import com.cognitionbox.petra.lang.Petra;

import java.util.function.Consumer;

@Edge
public class PopulateStrategies_for_testing implements Consumer<Strategy> {

    @Override
    public void accept(Strategy strategy) {
        // cannot mix and match props from different interface views within a condition, but can across conditions
        // this ensures only disjoint observations are made
        Petra.kases(strategy, Petra.kase(
                t->true,
                t->true,
                t->{

        }));
    }
}
