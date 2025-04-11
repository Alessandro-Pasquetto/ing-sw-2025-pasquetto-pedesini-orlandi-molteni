package org.progetto.server.controller.events;

import org.progetto.messages.toClient.Epidemic.AnotherPlayerCrewInfectedMessage;
import org.progetto.messages.toClient.Epidemic.CrewInfectedAmountMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.server.connection.Sender;
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
            ArrayList<Player> players = gameManager.getGame().getBoard().getCopyTravelers();

            System.out.println("Evaluating epidemic consequences");

            for (Player player : players) {

                // Calculates amount of crew infected, so removed
                int infectedCount = epidemic.epidemicResult(player);

                Sender sender = gameManager.getSenderByPlayer(player);

                sender.sendMessage(new CrewInfectedAmountMessage(infectedCount));
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerCrewInfectedMessage(infectedCount, player.getName()), sender);
            }

            // Checks for defeated players
            for (Player player : gameManager.getGame().getBoard().getCopyTravelers()) {

                if (player.getSpaceship().getTotalCrewCount() == 0) {
                    Sender sender = gameManager.getSenderByPlayer(player);

                    sender.sendMessage("NotEnoughCrew");
                    gameManager.broadcastGameMessage(new PlayerDefeatedMessage(player.getName()));
                    gameManager.getGame().getBoard().leaveTravel(player);
                }
            }
        }
    }
}