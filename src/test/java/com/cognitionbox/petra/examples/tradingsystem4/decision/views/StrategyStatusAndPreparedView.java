package com.cognitionbox.petra.examples.tradingsystem4.decision.views;

import com.cognitionbox.petra.annotations.Primative;

// having separate default interfaces means each can be analysed in isolation,
// ie allows for separation to reduce state space and therefore reduces complexity of abstract soundness/completeness
// checking, relative to the information provided in the interface only.
@Primative
public interface StrategyStatusAndPreparedView {
    StrategyPreparedView preparedView();
    StatusView statusView();
    default boolean isWaiting(){return preparedView().isPrepared() && statusView().isWaiting();}
    default boolean isDoSubmit(){return preparedView().isPrepared() && statusView().isDoSubmit();}
    default boolean isFailedToOpen(){return preparedView().isPrepared() && statusView().isFailedToOpen();}
    default boolean isOpen(){return preparedView().isPrepared() && statusView().isOpen();}
    default boolean isStopped(){return preparedView().isPrepared() && statusView().isStopped();}
    default boolean isExited(){return preparedView().isPrepared() && statusView().isExited();}
}
