package com.cognitionbox.petra.examples.tradingsystem4.marketdata.views;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.Instrument;

import java.util.List;

@Primative
public interface BidsView {
    List<Double> bids();
    default boolean bidsArrayIsSameSizeAsNumberOfInstruments(){
        return bids().size()== Instrument.values().length;
    }
}
