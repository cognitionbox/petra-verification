package com.cognitionbox.petra.examples.algorithmictrading.marketdata;

import com.cognitionbox.petra.examples.algorithmictrading.strategy.Decision;

public interface DecisionWithQuotes {
    Decision decision();
    Quotes quotes();
}
