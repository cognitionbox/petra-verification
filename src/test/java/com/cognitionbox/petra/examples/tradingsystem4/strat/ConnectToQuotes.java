package com.cognitionbox.petra.examples.tradingsystem4.strat;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.examples.tradingsystem4.decision.views.BrokerConnection;
import com.cognitionbox.petra.examples.tradingsystem4.marketdata.igindex.IGindexHelper;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class ConnectToQuotes implements Consumer<BrokerConnection> {

    @Override
    public void accept(BrokerConnection bc) {
        kases(bc,
            kase(
                    brokerConnection->brokerConnection.isNotConnected(),
                    brokerConnection->brokerConnection.isConnected(),
                    brokerConnection->{
                     // start market data code
                        IGindexHelper.login();
                        brokerConnection.connect();
            })
        );
    }
}
