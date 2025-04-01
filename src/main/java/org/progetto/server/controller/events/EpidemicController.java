package org.progetto.server.controller.events;

import org.progetto.server.connection.games.GameCommunicationHandler;
import org.progetto.server.controller.EventController;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.Epidemic;

import java.util.ArrayList;

public class EpidemicController extends EventController {

    // =======================
    // ATTRIBUTES
    // =======================

    GameCommunicationHandler gameCommunicationHandler;

    // =======================
    // CONSTRUCTORS
    // =======================

    public EpidemicController(GameCommunicationHandler gameCommunicationHandler) {
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
        ArrayList<Player> players = gameCommunicationHandler.getGame().getBoard().getActiveTravelers();

        for (Player player : players) {
            Epidemic epidemic = (Epidemic) gameCommunicationHandler.getGame().getActiveEventCard();
            epidemic.epidemicResult(player);
        }
    }
}