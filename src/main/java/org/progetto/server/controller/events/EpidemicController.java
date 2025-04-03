package org.progetto.server.controller.events;

import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventController;
import org.progetto.server.controller.LobbyController;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.Epidemic;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class EpidemicController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private GameManager gameManager;
    private String phase;

    // =======================
    // CONSTRUCTORS
    // =======================

    public EpidemicController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.phase = "START";
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Starts event card effect
     *
     * @author Gabriele
     */
    @Override
    public void start() throws RemoteException {
        if (phase.equals("START")) {
            phase = "EVENT_EFFECT";
            eventEffect();
        }
    }

    /**
     * Resolves event effect for each active traveler
     *
     * @author Gabriele
     */
    private void eventEffect() throws RemoteException {
        if (phase.equals("EVENT_EFFECT")) {
            ArrayList<Player> players = gameManager.getGame().getBoard().getActivePlayers();

            for (Player player : players) {
                Epidemic epidemic = (Epidemic) gameManager.getGame().getActiveEventCard();
                epidemic.epidemicResult(player);
            }

            phase = "END";
            end();
        }
    }

    /**
     * Send a message of end card to all players
     *
     * @author Stefano
     * @throws RemoteException
     */
    private void end() throws RemoteException {
        if (phase.equals("END")) {
            LobbyController.broadcastLobbyMessage("This event card is finished");
        }
    }
}