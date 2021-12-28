package com.cognitionbox.petra.examples.tradingsystem4.decision.views;

import com.cognitionbox.petra.annotations.Primative;

import java.time.LocalTime;

@Primative
public interface USOpenView {
    default boolean isUSOpen(){
            return LocalTime.now().isAfter(LocalTime.of(14,30)) && LocalTime.now().isBefore(LocalTime.of(15,30));}
    default boolean isNotUSOpen(){return !isUSOpen();}
}
