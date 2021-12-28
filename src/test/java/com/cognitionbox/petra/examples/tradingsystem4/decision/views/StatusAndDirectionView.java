package com.cognitionbox.petra.examples.tradingsystem4.decision.views;

import com.cognitionbox.petra.annotations.Primative;

// having separate default interfaces means each can be analysed in isolation,
// ie allows for separation to reduce state space and therefore reduces complexity of abstract soundness/completeness
// checking, relative to the information provided in the interface only.
@Primative
public interface StatusAndDirectionView extends StatusView, DirectionView {
    default boolean isWaitingAndBuy(){return isWaiting() && isBuy();}
    default boolean isWaitingAndSell(){return isWaiting() && isSell();}
    default boolean isOpenAndBuy(){return isOpen() && isBuy();}
    default boolean isOpenAndSell(){return isOpen() && isSell();}
    default boolean isStoppedAndBuy(){return isStopped() && isBuy();}
    default boolean isStoppedAndSell(){return isStopped() && isSell();}
    default boolean isExitedAndBuy(){return isExited() && isBuy();}
    default boolean isExitedAndSell(){return isExited() && isSell();}
}
