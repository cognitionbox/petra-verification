package com.cognitionbox.petra.examples.tradingsystem4.decision.submit;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.examples.tradingsystem4.decision.StrategyState;
import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.Submission;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class MarkDoNotSubmit implements Consumer<StrategyState> {

    @Override
    public void accept(StrategyState s) {
        kases(s, kase(
                strategyState->strategyState.isWaitingAndShouldNotBuy() ^ strategyState.isWaitingAndShouldNotSell(),
                strategyState->strategyState.doNotSubmit(),
                strategyState->strategyState.submission().set(Submission.DO_NOT_SUBIT)));
    }
}
