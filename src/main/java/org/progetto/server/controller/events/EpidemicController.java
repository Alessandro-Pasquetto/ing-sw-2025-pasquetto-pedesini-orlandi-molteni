package org.progetto.server.controller.events;

import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventController;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.Epidemic;

import java.util.ArrayList;

public class EpidemicController extends EventController {

    // =======================
    // ATTRIBUTES
    // =======================

    private GameManager gameManager;

    // =======================
    // CONSTRUCTORS
    // =======================

    public EpidemicController(GameManager gameManager) {
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
        ArrayList<Player> players = gameManager.getGame().getBoard().getActivePlayers();

        for (Player player : players) {
            Epidemic epidemic = (Epidemic) gameManager.getGame().getActiveEventCard();
            epidemic.epidemicResult(player);
        }
    }
}