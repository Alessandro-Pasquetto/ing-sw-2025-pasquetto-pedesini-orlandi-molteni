package org.progetto.server.controller;

import org.progetto.messages.toClient.Building.PickedEventCardMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.EventCard;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Event phase controller class
 */
public class EventController {

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Handles decision to pick an eventCard
     *
     * @author Lorenzo
     * @param gameManager is the class that manage the current game
     * @throws RemoteException if the eventCard can't be picked
     */
    public static void pickEventCard(GameManager gameManager) throws RemoteException, IllegalStateException, InterruptedException {

        EventCard card = gameManager.getGame().pickEventCard();

        gameManager.broadcastGameMessage(new PickedEventCardMessage(card));

        gameManager.getGameThread().notifyThread();
    }

    public static void handleDefeatedPlayers(GameManager gameManager) throws RemoteException, IllegalStateException, InterruptedException {

        Board board = gameManager.getGame().getBoard();

        // Checks for lapped player
        ArrayList<Player> lappedPlayers = board.checkLappedPlayers();

        if (lappedPlayers != null) {
            for (Player lappedPlayer : lappedPlayers) {

                // Gets lapped player sender reference
                Sender sender = gameManager.getSenderByPlayer(lappedPlayer);

                sender.sendMessage("YouGotLapped");
                gameManager.broadcastGameMessageToOthers(new PlayerDefeatedMessage(lappedPlayer.getName()), sender);
                board.leaveTravel(lappedPlayer);
            }
        }

        // Checks for players without crew
        ArrayList<Player> noCrewPlayers = board.checkNoCrewPlayers();

        if (noCrewPlayers != null) {
            for (Player noCrewPlayer : noCrewPlayers) {

                // Gets lapped player sender reference
                Sender sender = gameManager.getSenderByPlayer(noCrewPlayer);

                sender.sendMessage("YouHaveNoCrew");
                gameManager.broadcastGameMessageToOthers(new PlayerDefeatedMessage(noCrewPlayer.getName()), sender);
                board.leaveTravel(noCrewPlayer);
            }
        }
    }
}