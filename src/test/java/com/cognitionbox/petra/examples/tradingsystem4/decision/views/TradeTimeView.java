package com.cognitionbox.petra.examples.tradingsystem4.decision.views;

import com.cognitionbox.petra.annotations.Primative;

@Primative
public interface TradeTimeView extends WeekendView, EuropeOpenView{
    default boolean timeOk(){
            return isWeekend() && isEuropeOpen();}
    default boolean timeNotOk(){return !timeOk();}
}
