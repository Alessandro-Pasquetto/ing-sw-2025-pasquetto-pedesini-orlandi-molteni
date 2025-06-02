package org.progetto.server.controller.events;

import org.progetto.messages.toClient.Epidemic.AnotherPlayerCrewInfectedMessage;
import org.progetto.messages.toClient.Epidemic.CrewInfectedAmountMessage;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.Epidemic;

import java.util.ArrayList;

public class EpidemicController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Epidemic epidemic;

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
    public void start(){
        if (!phase.equals(EventPhase.START))
            throw new IllegalStateException("IncorrectPhase");

        phase = EventPhase.EFFECT;
        eventEffect();
    }

    /**
     * Resolves event effect for each active traveler
     *
     * @author Gabriele
     */
    private void eventEffect() {
        if (!phase.equals(EventPhase.EFFECT))
            throw new IllegalStateException("IncorrectPhase");

        ArrayList<Player> activePlayers = gameManager.getGame().getBoard().getCopyTravelers();

        for (Player player : activePlayers) {

            // Calculates amount of crew infected, so removed
            int infectedCount = epidemic.epidemicResult(player);

            Sender sender = gameManager.getSenderByPlayer(player);

            MessageSenderService.sendMessage(new CrewInfectedAmountMessage(infectedCount), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerCrewInfectedMessage(infectedCount, player.getName()), sender);

            // Updates player spaceship
            gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));
        }
    }
}