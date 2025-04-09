package org.progetto.server.controller.events;

import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.messages.toClient.PlayerMovedBackwardMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
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

    private GameManager gameManager;
    private Stardust stardust;
    private String phase;

    // =======================
    // CONSTRUCTORS
    // =======================

    public StardustController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.stardust = (Stardust) gameManager.getGame().getActiveEventCard();
        this.phase = "START";
    }

    // =======================
    // GETTERS
    // =======================

    @Override
    public String getPhase() throws RemoteException {
        return phase;
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
        if (phase.equals("START")) {
            phase = "EFFECT";
            eventEffect();
        }
    }

    /**
     * Resolves event effect for each active traveler
     *
     * @author Gabriele
     */
    private void eventEffect() throws RemoteException {
        if (phase.equals("EFFECT")) {
            ArrayList<Player> reversedPlayers = new ArrayList<>(gameManager.getGame().getBoard().getCopyActivePlayers());
            Collections.reverse(reversedPlayers);
            Board board = gameManager.getGame().getBoard();

            for (Player player : reversedPlayers) {
                int exposedConnectorsCount = stardust.penalty(board, player);

                // Sends update message
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
            gameManager.broadcastGameMessage("This event card is finished");
        }
    }
}
