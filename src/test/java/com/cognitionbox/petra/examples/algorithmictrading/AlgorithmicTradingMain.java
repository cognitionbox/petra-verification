package com.cognitionbox.petra.examples.algorithmictrading;

import com.cognitionbox.petra.examples.algorithmictrading.system.RunTradingSystem;
import com.cognitionbox.petra.examples.algorithmictrading.system.TradingSystem;
import com.cognitionbox.petra.examples.algorithmictrading.system.TradingSystemImpl;

import java.io.IOException;

import static com.cognitionbox.petra.lang.Petra.*;

public class AlgorithmicTradingMain {

    public static void main(String... args){

        TradingSystem ts = new TradingSystemImpl();
        ts.mode().mode().set(false);

        //finiteStart(RunTradingSystem::accept,ts,600,0);

        try {
            ts.csvDecisionWriter().fileWriter().get().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
