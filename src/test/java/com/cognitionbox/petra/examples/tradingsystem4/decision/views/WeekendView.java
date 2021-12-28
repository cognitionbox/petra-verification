package com.cognitionbox.petra.examples.tradingsystem4.decision.views;

import com.cognitionbox.petra.annotations.Primative;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Primative
public interface WeekendView {
    default boolean isWeekend(){
            return LocalDate.now().getDayOfWeek()!=DayOfWeek.SATURDAY && LocalDate.now().getDayOfWeek()!=DayOfWeek.SUNDAY;}
    default boolean isnotWeekend(){return !isWeekend();}
    default boolean kjasdh(){return isnotWeekend();}
}
