package com.cognitionbox.petra.examples.algorithmictrading.system;

import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.examples.algorithmictrading.marketdata.CsvMarketData;
import com.cognitionbox.petra.examples.algorithmictrading.marketdata.Quotes;
import com.cognitionbox.petra.examples.algorithmictrading.strategy.Decision;
import com.cognitionbox.petra.examples.algorithmictrading.strategy.Sma;
import com.cognitionbox.petra.examples.algorithmictrading.marketdata.Load;
import com.cognitionbox.petra.examples.algorithmictrading.orders.CsvDecisionWriter;

@View
public interface TradingSystem {
    CsvMarketData csvMarketData();
    CsvDecisionWriter csvDecisionWriter();
    Mode mode();
    Quotes quotes();
    Decision decision();
    Load load();
    Sma twenty();
    Sma fifty();
    Sma oneHundred();
    Sma twoHundred();
}
