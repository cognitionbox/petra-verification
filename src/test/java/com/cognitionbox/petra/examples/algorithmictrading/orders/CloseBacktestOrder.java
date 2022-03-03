package com.cognitionbox.petra.examples.algorithmictrading.orders;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge public class CloseBacktestOrder implements Consumer<DecisionWriter> {
    @Override
    public void accept(DecisionWriter d) {
        kases(d,
                kase(decisionWriter->true, decisionWriter->true, decisionWriter->{
                    decisionWriter.csvDecisionWriter().writeCloseOrder(d.decision());
                })
        );
    }
}
