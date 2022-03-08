package com.cognitionbox.petra.examples.algorithmictrading.marketdata;

import com.cognitionbox.petra.lang.step.PEdge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

 public class UpdateDecisionQuoteUsingLiveData implements PEdge<DecisionWithQuotes> {
    @Override
    public void accept(DecisionWithQuotes d) {
        kases(d,
                kase(decisionWithQuotes->decisionWithQuotes.quotes().goodQuote(),
                     decisionWithQuotes->
                             decisionWithQuotes.decision().bid().isUpdated() &&
                             decisionWithQuotes.decision().ask().isUpdated()
                        , decisionWithQuotes->{

                })
        );
    }
}
