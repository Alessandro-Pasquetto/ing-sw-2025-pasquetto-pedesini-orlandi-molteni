package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import static org.junit.jupiter.api.Assertions.*;

class OpenSpaceTest {

    @Test
    void moveAhead() {
        Game game = new Game(0, 3, 2);
        Board board = game.getBoard();
        Player player = new Player("gino");

        game.addPlayer(player);
        game.initPlayersSpaceship();

        board.addTraveler(player);
        board.addTravelersOnTrack(1);

        OpenSpace openspace = new OpenSpace(CardType.OPENSPACE, 2 , "imgPath");

        // Calls moveAhead method
        openspace.moveAhead(board, player, 5);

        assertEquals(player, board.getTrack()[9]);
    }
}