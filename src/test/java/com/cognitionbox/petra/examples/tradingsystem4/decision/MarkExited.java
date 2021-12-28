package com.cognitionbox.petra.examples.tradingsystem4.decision;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.Status;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class MarkExited implements Consumer<Strategy> {

    @Override
    public void accept(Strategy strategy) {
        kases(strategy,
                kase(s->s.strategyState().isOpen(), s->s.strategyState().isExited(), s->{
                    s.strategyState().status().set(Status.EXITED);
                })
        );
    }
}
