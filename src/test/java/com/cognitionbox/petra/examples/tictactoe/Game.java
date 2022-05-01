package com.cognitionbox.petra.examples.tictactoe;

import com.cognitionbox.petra.lang.step.PGraph;

import java.awt.*;

import static com.cognitionbox.petra.examples.tictactoe.CellState.CROSS;
import static com.cognitionbox.petra.examples.tictactoe.CellState.NOTHING;
import static com.cognitionbox.petra.lang.Petra.*;

public interface Game extends GameState {
    // allow params to be used in this object, ie bring them into the product
    default void handlePlayer1Move(Player1 p){
        kases(p,
                kase(player1->
                player1.player1().peek().state()==NOTHING &&
                player1.player1().peek().cellIndex().get()>=0 &&
                player1.player1().peek().cellIndex().get()<player1.grid().cells().size(),
                player1->true,
                player1->{
                    seqr(player1.grid().lines(), Game::processLine);
                })
        );
    }

    static void processLine(Line l){
        kases(l,
                kase(line->true,
                    line->true,
                    game->{
                        seqr(l.cells(), Game::processCell);
                    })
        );
    }

    static void processCell(Cell c){
        kases(c,
                kase(cell->true,
                        cell->true,
                game->{

                }));
    }

    default void giveAllCellsEvent(){
        kases(this,
                kase(game->true,
                        game->game.grid().cells().forall(c->c.event().isPresent()),
                        game->{

                        })
        );
    }

    default void handleAll(){
        kases(this,
                kase(game-> game.event().get().state().get()==CROSS,
                        game->true,
                        game->{
                            state().set(CellState.CROSS);
                        })
        );
    }

    default void handleCross(){
        kases(this,
                kase(game-> game.event().get().state().get()==CROSS,
                        game->true,
                        game->{
                            Cell cell = make(Cell.class);
                            cell.state().set(CROSS);
                            game.grid().cells().set(game.event().get().index().get(), cell);
                        })
        );
    }
}
