package com.cognitionbox.petra.examples.algorithmictrading.strategy;

import com.cognitionbox.petra.examples.algorithmictrading.marketdata.Quote;
import com.cognitionbox.petra.lang.primitives.impls.PBigDecimal;

import java.util.ArrayDeque;
import java.util.Queue;

import static com.cognitionbox.petra.lang.primitives.PBigDecimals.TWO;

public final class Sma {
    final private Queue<Quote> quotes = new ArrayDeque<>();
    final int window;

    public Sma(int window){
        this.window = window;
    }

    public void add(Quote q){
        if (quotes.size()<window){
            quotes.offer(q);
        } else {
            quotes.poll();
            quotes.offer(q);
        }
    }

    public PBigDecimal calc(){
        PBigDecimal sum = quotes.stream()
                .map(d->d.bid().add(d.ask()).divide(TWO))
                .reduce((a,b)->a.add(b)).get();
        return sum.divide(new PBigDecimal(quotes.size()));
    }
}
