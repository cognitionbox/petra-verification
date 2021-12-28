package com.cognitionbox.petra.examples.tradingsystem4.decision.views;

import com.cognitionbox.petra.annotations.Primative;

import java.time.LocalTime;

@Primative
public interface EuropeOpenView {
    default boolean isEuropeOpen(){
            return LocalTime.now().isAfter(LocalTime.of(8,0)) && LocalTime.now().isBefore(LocalTime.of(9,0));}
    default boolean isNotEuropeOpen(){return !isEuropeOpen();}
}
