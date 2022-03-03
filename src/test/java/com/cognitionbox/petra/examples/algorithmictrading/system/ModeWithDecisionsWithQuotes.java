package com.cognitionbox.petra.examples.algorithmictrading.system;

import com.cognitionbox.petra.examples.algorithmictrading.marketdata.DecisionWithQuotes;
import com.cognitionbox.petra.examples.algorithmictrading.marketdata.Quotes;
import com.cognitionbox.petra.examples.algorithmictrading.strategy.Decision;

public interface ModeWithDecisionsWithQuotes extends DecisionWithQuotes {
    Mode mode();
    Decision decision();
    Quotes quotes();
}
