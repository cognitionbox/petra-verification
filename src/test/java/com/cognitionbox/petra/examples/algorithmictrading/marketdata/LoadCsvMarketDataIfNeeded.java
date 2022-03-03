package com.cognitionbox.petra.examples.algorithmictrading.marketdata;

import java.io.IOException;
import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

public class LoadCsvMarketDataIfNeeded implements Consumer<Load> {
    @Override
    public void accept(Load c) {
        kases(c,
            kase(csv->csv.isNotLoaded(), csv->csv.isLoaded(), csv->{
                try {
                    csv.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }),
            kase(csv->csv.isLoaded(), csv->csv.isLoaded(), csv->{
                //
            })
        );
    }
}
