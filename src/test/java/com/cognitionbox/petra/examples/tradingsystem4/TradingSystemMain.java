package com.cognitionbox.petra.examples.tradingsystem4;

import com.cognitionbox.petra.lang.Petra;

public class TradingSystemMain {
    public static void main(String... args){
        Petra.infiniteStart(new RunTradingSystem(),new TradingSystem(), 5000);
    }
}
