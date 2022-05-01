package com.cognitionbox.petra.examples.tictactoe;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.lang.collection.PCollection;
import com.cognitionbox.petra.lang.collection.PList;

import java.util.Collection;

public interface Grid {
    PList<Cell> cells();
    PCollection<Line> lines();
}
