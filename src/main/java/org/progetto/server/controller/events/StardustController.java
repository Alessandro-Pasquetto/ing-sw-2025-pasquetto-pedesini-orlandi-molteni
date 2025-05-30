package org.progetto.server.controller.events;

import org.progetto.messages.toClient.EventGeneric.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventGeneric.PlayerMovedBackwardMessage;
import org.progetto.messages.toClient.Stardust.ExposedConnectorsMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.Stardust;

import java.util.ArrayList;
import java.util.Collections;

public class StardustController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Stardust stardust;

    // =======================
    // CONSTRUCTORS
    // =======================

    public StardustController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.stardust = (Stardust) gameManager.getGame().getActiveEventCard();
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

        ArrayList<Player> reversedPlayers = gameManager.getGame().getBoard().getCopyTravelers();
        Collections.reverse(reversedPlayers);
        Board board = gameManager.getGame().getBoard();

        System.out.println("Evaluating stardust consequences");

        for (Player player : reversedPlayers) {

            // Calculates exposed connector count
            int exposedConnectorsCount = stardust.penalty(board, player);

            Sender sender = gameManager.getSenderByPlayer(player);

            MessageSenderService.sendOptional(new ExposedConnectorsMessage(exposedConnectorsCount), sender);

            MessageSenderService.sendOptional(new PlayerMovedBackwardMessage(exposedConnectorsCount), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedBackwardMessage(player.getName(), exposedConnectorsCount), sender);
        }
    }
}
