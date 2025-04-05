package org.progetto.server.controller.events;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.PlayerMovedBackwardMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.controller.LobbyController;
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
    private String phase;

    // =======================
    // CONSTRUCTORS
    // =======================

    public StardustController(GameManager gameManager) {
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
            ArrayList<Player> reversedPlayers = new ArrayList<>(gameManager.getGame().getBoard().getActivePlayers());
            Collections.reverse(reversedPlayers);
            Board board = gameManager.getGame().getBoard();

            for (Player player : reversedPlayers) {
                Stardust stardust = (Stardust) gameManager.getGame().getActiveEventCard();
                int exposedConnectorsCount = stardust.penalty(board, player);

                // Sends update message
                SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(player);
                VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(player);

                Sender sender = null;

                if (socketWriter != null) {
                    sender = socketWriter;
                } else if (virtualClient != null) {
                    sender = virtualClient;
                }

                sender.sendMessage(new PlayerMovedBackwardMessage(exposedConnectorsCount));
                LobbyController.broadcastLobbyMessageToOthers(new AnotherPlayerMovedBackwardMessage(player.getName(), exposedConnectorsCount), sender);
            }

            board.updateTurnOrder();

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
