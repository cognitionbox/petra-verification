package com.cognitionbox.petra.examples.algorithmictrading.orders;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.examples.algorithmictrading.strategy.Decision;
import com.cognitionbox.petra.lang.primitives.PValue;

import java.io.FileWriter;
import java.io.IOException;

@Primative
@View
public interface CsvDecisionWriter {
    PValue<FileWriter> fileWriter();

    default boolean isReady(){
        return fileWriter().get()!=null;
    }

    default boolean isNotReady(){
        return fileWriter().get()==null;
    }

    default void init(){
        try {
            fileWriter().set(new FileWriter("C:\\Users\\aranh\\OneDrive\\Documents\\XAUUSD-decisions2.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    default void writeOpenOrder(Decision decision){
        try {
            fileWriter().get().write(
                    decision.symbol().get()+","+decision.bid().get()+","+decision.ask().get()+","+"Open"+","+(decision.isLong().get()?"Buy":"Sell")+","+decision.qty().get()+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    default void writeCloseOrder(Decision decision){
        try {
            fileWriter().get().write(
                    decision.symbol().get()+","+decision.bid().get()+","+decision.ask().get()+","+"Close"+","+(decision.isLong().get()?"Buy":"Sell")+","+decision.qty().get()+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
