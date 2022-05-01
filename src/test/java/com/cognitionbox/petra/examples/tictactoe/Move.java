package com.cognitionbox.petra.examples.tictactoe;

import com.cognitionbox.petra.lang.primitives.impls.PInteger;

public interface Move {
    // instruction
    PInteger cellIndex();
    CellState state();
}
