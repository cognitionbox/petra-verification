package com.cognitionbox.petra.examples.tradingsystem4.decision.views;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.lang.primitives.impls.PBoolean;

// having separate default interfaces means each can be analysed in isolation,
// ie allows for separation to reduce state space and therefore reduces complexity of abstract soundness/completeness
// checking, relative to the information provided in the interface only.
@Primative
public interface BrokerConnection {
    PBoolean connected();
    default void connect(){connected().set(true);}
    default boolean isConnected(){return connected().get();}
    default boolean isNotConnected(){return !isConnected();}
}
