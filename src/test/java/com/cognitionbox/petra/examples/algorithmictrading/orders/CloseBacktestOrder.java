package com.cognitionbox.petra.examples.algorithmictrading.orders;

import com.cognitionbox.petra.lang.step.PEdge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

 public class CloseBacktestOrder implements PEdge<DecisionWriter> {
    @Override
    public void accept(DecisionWriter d) {
        kases(d,
                kase(decisionWriter->true, decisionWriter->true, decisionWriter->{
                    decisionWriter.csvDecisionWriter().writeCloseOrder(d.decision());
                })
        );
    }
}
