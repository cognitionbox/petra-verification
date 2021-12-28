package com.cognitionbox.petra.examples.tradingsystem4.decision.views;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.PStatus;
import com.cognitionbox.petra.examples.tradingsystem4.decision.enums.Status;

// having separate default interfaces means each can be analysed in isolation,
// ie allows for separation to reduce state space and therefore reduces complexity of abstract soundness/completeness
// checking, relative to the information provided in the interface only.
@Primative
public interface StatusView {
    PStatus status();
    default boolean isWaiting(){return status().get()== Status.WAITING;}
    default boolean isDoSubmit(){return status().get()== Status.DO_SUBMIT;}
    default boolean isFailedToOpen(){return status().get()== Status.FAILED_TO_OPEN;}
    default boolean isOpen(){return status().get()== Status.OPENED;}
    default boolean isStopped(){return status().get()== Status.STOPPED;}
    default boolean isExited(){return status().get()== Status.EXITED;}
}
