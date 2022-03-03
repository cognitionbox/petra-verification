package com.cognitionbox.petra.examples.algorithmictrading.orders;

import com.cognitionbox.petra.examples.algorithmictrading.strategy.Decision;

public interface DecisionWriter {
    Decision decision();
    CsvDecisionWriter csvDecisionWriter();
}
