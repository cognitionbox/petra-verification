package com.cognitionbox.petra.examples.algorithmictrading.marketdata;

import com.cognitionbox.petra.examples.algorithmictrading.orders.CsvDecisionWriter;

import java.io.IOException;
import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

public class CloseCsvDecisionWriter implements Consumer<CsvDecisionWriter> {
    @Override
    public void accept(CsvDecisionWriter c) {
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
