package com.cognitionbox.petra.examples.algorithmictrading.marketdata;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.lang.primitives.impls.PBigDecimal;

@Primative @View
public interface Quote {
    PBigDecimal bid();
    PBigDecimal ask();

    default boolean goodQuote(){
        return bid().get()!=null && ask().get()!=null && ask().gt(bid());
    }

    default boolean badQuote(){
        return (bid().get()==null || ask().get()==null) && ask().le(bid());
    }
}
