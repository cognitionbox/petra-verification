package com.cognitionbox.petra.examples.tradingsystem4.decision.enums;

public enum Instrument {
   //UNDECIDED(""), FTSE("IX.D.FTSE.DAILY.IP"), DAX("IX.D.DAX.DAILY.IP");
   UNDECIDED(""), SPTRD("IX.D.SPTRD.DAILY.IP");

   final public String epic;
   Instrument(String value){
      this.epic = value;
   }
}
