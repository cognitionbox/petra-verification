package com.cognitionbox.petra.examples.tradingsystem4.decision.views;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.examples.tradingsystem4.marketdata.views.AsksView;
import com.cognitionbox.petra.examples.tradingsystem4.marketdata.views.BidsView;
import com.cognitionbox.petra.examples.tradingsystem4.marketdata.views.StartedStatesView;

// having separate default interfaces means each can be analysed in isolation,
// ie allows for separation to reduce state space and therefore reduces complexity of abstract soundness/completeness
// checking, relative to the information provided in the interface only.
@Primative
public interface QuotesView extends AsksView, BidsView, StartedStatesView {
//    default boolean startedAndGoodQuote(InstrumentEnum i) {return started().get(i.ordinal()) && asks().get(i.ordinal()) > bids().get(i.ordinal());}
//    default boolean startedAndBadQuote(InstrumentEnum i) {return started().get(i.ordinal()) && asks().get(i.ordinal()) <= bids().get(i.ordinal());}
}
