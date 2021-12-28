package com.cognitionbox.petra.examples.trafficsignalling2;

import com.cognitionbox.petra.examples.tradingsystem4.RunTradingSystem;
import com.cognitionbox.petra.examples.tradingsystem4.TradingSystem;
import com.cognitionbox.petra.lang.Petra;

public class TrafficSignallingMain {
    public static void main(String... args){
        Petra.infiniteStart(new RunTradingSystem(),new TradingSystem(), 5000);
    }
}
