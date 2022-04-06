package com.cognitionbox.petra.examples.algorithmictrading.marketdata;

import com.cognitionbox.petra.examples.algorithmictrading.orders.CsvDecisionWriter;
import com.cognitionbox.petra.lang.step.PEdge;

import java.io.IOException;
import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

public interface CloseCsvDecisionWriter extends PEdge<CsvDecisionWriter> {
    static  void accept(CsvDecisionWriter c) {
        kases(c,
            kase(csv->csv.isReady(), csv->csv.isReady(), csv->{
                try {
                    csv.fileWriter().get().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            })
        );
    }
}
