package org.progetto.server.controller.events;

import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.Epidemic;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class EpidemicController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private Epidemic epidemic;

    // =======================
    // CONSTRUCTORS
    // =======================

    public EpidemicController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.epidemic = (Epidemic) gameManager.getGame().getActiveEventCard();
        this.currPlayer = -1;
        this.phase = EventPhase.START;
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
        if (phase.equals(EventPhase.START)) {
            phase = EventPhase.EFFECT;
            eventEffect();
        }
    }

    /**
     * Resolves event effect for each active traveler
     *
     * @author Gabriele
     */
    private void eventEffect() throws RemoteException {
        if (phase.equals(EventPhase.EFFECT)) {
            ArrayList<Player> players = gameManager.getGame().getBoard().getCopyActivePlayers();

            for (Player player : players) {
                epidemic.epidemicResult(player);
            }

            phase = EventPhase.END;
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
        if (phase.equals(EventPhase.END)) {
            gameManager.broadcastGameMessage("This event card is finished");
        }
    }
}