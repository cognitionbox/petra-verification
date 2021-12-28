package com.cognitionbox.petra.examples.tradingsystem4.marketdata;


import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.Instrument;
import com.cognitionbox.petra.examples.tradingsystem4.decision.views.QuotesView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Quotes implements QuotesView, Serializable {
    // each initializer like these could be modelled as an edge with a true post condition,
    // acting on a data type which wraps the primitive, and with post condition...
    private final List<Boolean> started = new ArrayList<>(Instrument.values().length);
    private final List<Double> bids = new ArrayList<>(Instrument.values().length);
    private final List<Double> asks = new ArrayList<>(Instrument.values().length);

    @Override
    public List<Double> bids() {
        return bids;
    }

    @Override
    public List<Double> asks() {
        return asks;
    }

    @Override
    public List<Boolean> started() {
        return started;
    }
}
