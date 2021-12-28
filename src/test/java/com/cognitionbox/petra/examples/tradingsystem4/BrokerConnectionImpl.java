package com.cognitionbox.petra.examples.tradingsystem4;

import com.cognitionbox.petra.examples.tradingsystem4.decision.views.BrokerConnection;
import com.cognitionbox.petra.lang.primitives.impls.PBoolean;

public class BrokerConnectionImpl implements BrokerConnection {
    PBoolean connected = new PBoolean();
    public PBoolean connected() { return connected; }
}
