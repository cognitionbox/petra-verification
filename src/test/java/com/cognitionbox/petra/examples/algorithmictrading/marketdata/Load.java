package com.cognitionbox.petra.examples.algorithmictrading.marketdata;

import com.cognitionbox.petra.annotations.View;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Queue;

@View
public interface Load {
    Queue<Quote> quotes();
    default boolean isNotLoaded(){
        return quotes().isEmpty();
    }
    default boolean isLoaded(){
        return !quotes().isEmpty();
    }
    default void load() throws IOException {
        String DELIMITER = ",";
        int BID_FIELD = 1;
        int ASK_FIELD = 2;
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\aranh\\OneDrive\\Documents\\XAUUSD-marketdata.csv"));
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] split = line.split(DELIMITER);
            Quote q = new QuoteImpl();
            BigDecimal bid = new BigDecimal(split[BID_FIELD]);
            BigDecimal ask = new BigDecimal(split[ASK_FIELD]);
            bid.setScale(2, BigDecimal.ROUND_CEILING);
            ask.setScale(2, BigDecimal.ROUND_CEILING);
            q.bid().set(bid);
            q.ask().set(ask);
            quotes().offer(q);
        }
    }
}
