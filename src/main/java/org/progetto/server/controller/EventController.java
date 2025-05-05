package org.progetto.server.controller;

import org.progetto.messages.toClient.Building.PickedEventCardMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.messages.toClient.EventCommon.PlayerLeftMessage;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipMessage;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipStatsMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.Board;
import org.progetto.server.model.GamePhase;
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
     * @throws IllegalStateException
     * @throws InterruptedException
     */
    public static void pickEventCard(GameManager gameManager) throws RemoteException, IllegalStateException, InterruptedException {

        EventCard card = gameManager.getGame().pickEventCard();
        for(Player player : gameManager.getGame().getPlayersCopy()){

            Sender sender = gameManager.getSenderByPlayer(player);
            sender.sendMessage(new ResponseSpaceshipMessage(player.getSpaceship(), player));
        }
        
        gameManager.broadcastGameMessage(new PickedEventCardMessage(card));
    }

    /**
     * Checks if there is any defeated player
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @throws RemoteException
     * @throws IllegalStateException
     * @throws InterruptedException
     */
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

    /**
     * Handles player decision to leave travel
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @param response player's response
     * @param player current player
     * @param sender current sender
     * @throws RemoteException
     */
    public static void chooseToContinueTravel(GameManager gameManager, String response, Player player, Sender sender) throws RemoteException {

        GamePhase gamePhase = gameManager.getGame().getPhase();
        Board board = gameManager.getGame().getBoard();

        // Checks if player can decide to leave travel
        if (gamePhase.equals(GamePhase.TRAVEL) && !player.getIsReady() && board.getCopyTravelers().contains(player)) {

            String upperCaseResponse = response.toUpperCase();

            switch (upperCaseResponse) {
                case "YES":
                    sender.sendMessage("YouAreContinuingTravel");

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                    break;

                case "NO":
                    sender.sendMessage("YouLeftTravel");
                    gameManager.broadcastGameMessageToOthers(new PlayerLeftMessage(player.getName()), sender);
                    board.leaveTravel(player);

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                    break;

                default:
                    sender.sendMessage("IncorrectResponse");
                    break;
            }

        } else {
            sender.sendMessage("NotAllowedToDoThat");
        }
    }
}