package com.cognitionbox.petra.examples.algorithmictrading.system;

import com.cognitionbox.petra.examples.algorithmictrading.marketdata.CsvMarketData;
import com.cognitionbox.petra.examples.algorithmictrading.marketdata.Load;
import com.cognitionbox.petra.examples.algorithmictrading.marketdata.CsvMarketDataImpl;
import com.cognitionbox.petra.examples.algorithmictrading.marketdata.Quotes;
import com.cognitionbox.petra.examples.algorithmictrading.orders.CsvDecisionWriter;
import com.cognitionbox.petra.examples.algorithmictrading.orders.CsvDecisionWriterImpl;
import com.cognitionbox.petra.examples.algorithmictrading.strategy.*;

public class TradingSystemImpl implements TradingSystem, ModeWithDecisionsWithQuotes{
    private final Decision decision = new DecisionImpl();
    private final Sma twenty = new Sma(20);
    private final Sma fifty = new Sma(50);
    private final Sma oneHundred = new Sma(100);
    private final Sma twoHundred = new Sma(200);
    private final Mode mode = new ModeImpl();
    private final CsvMarketData csvMarketData = new CsvMarketDataImpl();
    private final CsvDecisionWriter csvDecisionWriter = new CsvDecisionWriterImpl();

    @Override
    public CsvMarketData csvMarketData() {return csvMarketData;}

    @Override
    public CsvDecisionWriter csvDecisionWriter() {
        return csvDecisionWriter;
    }

    @Override
    public Mode mode() {return mode;}

    @Override
    public Quotes quotes() {
        return csvMarketData;
    }

    @Override
    public Load load() {
        return csvMarketData;
    }

    @Override
    public Decision decision() {
        return decision;
    }

    @Override
    public Sma twenty() {
        return twenty;
    }

    @Override
    public Sma fifty() {
        return fifty;
    }

    @Override
    public Sma oneHundred() {
        return oneHundred;
    }

    @Override
    public Sma twoHundred() {
        return twoHundred;
    }
}
