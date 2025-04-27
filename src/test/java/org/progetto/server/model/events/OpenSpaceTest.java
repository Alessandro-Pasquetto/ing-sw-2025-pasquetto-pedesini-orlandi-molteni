package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import static org.junit.jupiter.api.Assertions.*;

class OpenSpaceTest {

    @Test
    void moveAhead() {
        Board board = new Board(1);
        Player player = new Player("gino", 0, 1);

        board.addTraveler(player);
        board.addTravelersInTrack(1);

        OpenSpace openspace = new OpenSpace(CardType.OPENSPACE, 2 , "imgPath");

        // Calls moveAhead method
        openspace.moveAhead(board, player, 5);

        assertEquals(player, board.getTrack()[9]);
    }
}