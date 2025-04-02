package org.progetto.server.controller.events;

import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventController;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.Stardust;

import java.util.ArrayList;
import java.util.Collections;

public class StardustController extends EventController {

    // =======================
    // ATTRIBUTES
    // =======================

    private GameManager gameManager;

    // =======================
    // CONSTRUCTORS
    // =======================

    public StardustController(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Starts event card effect
     */
    public void start() {
        eventEffect();
    }

    /**
     * Resolves event effect for each active traveler
     *
     * @author Gabriele
     */
    private void eventEffect() {
        ArrayList<Player> reversedPlayers = new ArrayList<>(gameManager.getGame().getBoard().getActivePlayers());
        Collections.reverse(reversedPlayers);
        Board board = gameManager.getGame().getBoard();

        for (Player player : reversedPlayers) {
            Stardust stardust = (Stardust) gameManager.getGame().getActiveEventCard();
            stardust.penalty(board, player);
        }

        board.updateTurnOrder();
    }
}
