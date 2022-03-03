package com.cognitionbox.petra.examples.algorithmictrading.marketdata;

import com.cognitionbox.petra.annotations.View;

import java.util.Queue;

@View
public interface Quotes {
    Queue<Quote> quotes();
    default boolean goodQuote(){
        return
                quotes().peek()!=null &&
                quotes().peek().bid().get()!=null &&
                quotes().peek().ask().get()!=null &&
                quotes().peek().ask().gt(quotes().peek().bid());
    }
    default boolean badQuote(){
        return !goodQuote();
    }

    default Quote consume(){
        return quotes().poll();
    }
}
