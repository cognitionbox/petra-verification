package com.cognitionbox.petra.examples.tictactoe;

import com.cognitionbox.petra.lang.collection.PQueue;

public interface MovesQueue {
    PQueue<Move> events();
    default Move peek(){
        return events().peek();
    }
}
