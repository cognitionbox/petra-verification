package com.cognitionbox.petra.examples.algorithmictrading.marketdata;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge public class UpdateDecisionQuoteUsingHistoricalCsvData implements Consumer<DecisionWithQuotes> {
    @Override
    public void accept(DecisionWithQuotes d) {
        kases(d,
                kase(decisionWithQuotes->decisionWithQuotes.quotes().goodQuote(),
                     decisionWithQuotes->
                             decisionWithQuotes.decision().bid().isUpdated() &&
                             decisionWithQuotes.decision().ask().isUpdated()
                        , decisionWithQuotes->{
                    Quote q = decisionWithQuotes.quotes().consume();
                    decisionWithQuotes.decision().bid().set(q.bid().get());
                    decisionWithQuotes.decision().ask().set(q.ask().get());
                })
        );
    }
}
