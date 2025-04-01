package org.progetto.server.controller.events;

import org.progetto.server.connection.games.GameCommunicationHandler;
import org.progetto.server.controller.EventController;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.Epidemic;
import org.progetto.server.model.events.Stardust;

import java.util.ArrayList;
import java.util.Collections;

public class StardustController extends EventController {

    // =======================
    // ATTRIBUTES
    // =======================

    GameCommunicationHandler gameCommunicationHandler;

    // =======================
    // CONSTRUCTORS
    // =======================

    public StardustController(GameCommunicationHandler gameCommunicationHandler) {
        this.gameCommunicationHandler = gameCommunicationHandler;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Resolves event effect for each active traveler
     *
     * @author Gabriele
     */
    public void eventEffect() {
        ArrayList<Player> reversedPlayers = new ArrayList<>(gameCommunicationHandler.getGame().getBoard().getActiveTravelers());
        Collections.reverse(reversedPlayers);
        Board board = gameCommunicationHandler.getGame().getBoard();

        for (Player player : reversedPlayers) {
            Stardust stardust = (Stardust) gameCommunicationHandler.getGame().getActiveEventCard();
            stardust.penalty(board, player);
        }

        board.updateTurnOrder();
    }
}
