package com.cognitionbox.petra.examples.algorithmictrading.marketdata;

import java.util.ArrayDeque;
import java.util.Queue;

public class CsvMarketDataImpl implements CsvMarketData {
    private final Queue<Quote> quotes = new ArrayDeque<>();
    @Override
    public Queue<Quote> quotes() {
        return quotes;
    }
}
