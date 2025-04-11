package org.progetto.server.controller.events;

import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.messages.toClient.PlayerMovedBackwardMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.Stardust;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;

public class StardustController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private Stardust stardust;

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
     * @throws RemoteException
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
            ArrayList<Player> reversedPlayers = gameManager.getGame().getBoard().getCopyTravelers();
            Collections.reverse(reversedPlayers);
            Board board = gameManager.getGame().getBoard();

            System.out.println("Evaluating stardust consequences");

            for (Player player : reversedPlayers) {

                // Calculates exposed connector count
                int exposedConnectorsCount = stardust.penalty(board, player);

                Sender sender = gameManager.getSenderByPlayer(player);

                sender.sendMessage(new PlayerMovedBackwardMessage(exposedConnectorsCount));
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedBackwardMessage(player.getName(), exposedConnectorsCount), sender);
            }

            // Updates turn order
            board.updateTurnOrder();

            // Checks for lapped player
            ArrayList<Player> lappedPlayers = board.checkLappedPlayers();

            if (lappedPlayers != null) {
                for (Player lappedPlayer : lappedPlayers) {

                    // Gets lapped player sender reference
                    Sender senderLapped = gameManager.getSenderByPlayer(lappedPlayer);

                    senderLapped.sendMessage("YouGotLapped");
                    gameManager.broadcastGameMessageToOthers(new PlayerDefeatedMessage(lappedPlayer.getName()), senderLapped);
                    board.leaveTravel(lappedPlayer);
                }
            }
        }
    }
}
