package com.cognitionbox.petra.examples.tictactoe;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.lang.primitives.PValue;
import com.cognitionbox.petra.lang.primitives.impls.PInteger;

import static com.cognitionbox.petra.lang.Petra.*;

public interface Cell {
    PInteger index();
    PValue<CellState> state();

    default boolean naught(){
        return state().get()== CellState.NAUGHT;
    }

    default boolean cross(){
        return state().get()== CellState.CROSS;
    }

    default boolean nothing(){
        return state().get()== CellState.NOTHING;
    }

    @Edge default void handleEvent(Move e){
        kases(this,
                kase(cell->
                        cell.nothing() &&
                        e.state()==CellState.CROSS &&
                        e.cellIndex().get()==cell.index().get() , cell->cell.cross(), light->{
                    state().set(CellState.CROSS);
                }),
                kase(cell->
                        cell.nothing() &&
                        e.state()==CellState.NAUGHT &&
                        e.cellIndex().get()==cell.index().get() , cell->cell.cross(), light->{
                    state().set(CellState.NAUGHT);
                }),
                kase(cell->!cell.nothing(),cell->!cell.nothing(),light->{
                    //
                })
        );
    }
}
