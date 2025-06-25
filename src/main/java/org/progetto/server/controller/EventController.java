package org.progetto.server.controller;

import org.progetto.messages.toClient.Building.PickedEventCardMessage;
import org.progetto.messages.toClient.EventGeneric.PlayerDefeatedMessage;
import org.progetto.messages.toClient.Track.UpdateTrackMessage;
import org.progetto.messages.toClient.Travel.PlayerIsContinuingMessage;
import org.progetto.messages.toClient.Travel.PlayerLeftMessage;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.Board;
import org.progetto.server.model.Game;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.EventCard;

import java.util.ArrayList;


public class EventController {

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Handles decision to pick an eventCard
     *
     * @author Lorenzo
     * @param gameManager is the class that manage the current game
     * @throws IllegalStateException
     */
    public static void pickEventCard(GameManager gameManager) throws IllegalStateException {

        EventCard card = gameManager.getGame().pickEventCard();

        for(Player player : gameManager.getGame().getPlayersCopy()){
            Sender sender = gameManager.getSenderByPlayer(player);
            MessageSenderService.sendMessage(new ResponseSpaceshipMessage(player.getSpaceship(), player), sender);
        }

        gameManager.broadcastGameMessage(new PickedEventCardMessage(card));
    }

    /**
     * Checks if there is any defeated player
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @throws IllegalStateException
     */
    public static void handleDefeatedPlayers(GameManager gameManager) throws IllegalStateException {

        Board board = gameManager.getGame().getBoard();

        ArrayList<Player> playersInTrack = GameController.getAllPlayersInTrackCopy(gameManager);

        if(playersInTrack.isEmpty())
            return;

        // Checks for lapped player
        ArrayList<Player> lappedPlayers = board.checkLappedPlayers(playersInTrack);

        if (lappedPlayers != null) {
            for (Player lappedPlayer : lappedPlayers) {

                // Gets lapped player sender reference
                Sender sender = gameManager.getSenderByPlayer(lappedPlayer);

                MessageSenderService.sendMessage("YouGotLapped", sender);
                gameManager.broadcastGameMessageToOthers(new PlayerDefeatedMessage(lappedPlayer.getName()), sender);
            }
        }

        // Checks for players without crew
        ArrayList<Player> noCrewPlayers = board.checkNoCrewPlayers(playersInTrack);

        if (noCrewPlayers != null) {
            for (Player noCrewPlayer : noCrewPlayers) {

                // Gets no crew player sender reference
                Sender sender = gameManager.getSenderByPlayer(noCrewPlayer);

                MessageSenderService.sendMessage("YouHaveNoCrew", sender);
                gameManager.broadcastGameMessageToOthers(new PlayerDefeatedMessage(noCrewPlayer.getName()), sender);
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
     */
    public static void chooseToContinueTravel(GameManager gameManager, String response, Player player, Sender sender) {

        GamePhase gamePhase = gameManager.getGame().getPhase();
        Board board = gameManager.getGame().getBoard();

        // Checks if player can decide to leave travel
        if (gamePhase.equals(GamePhase.TRAVEL) && !player.getIsReady() && GameController.getAllPlayersInTrackCopy(gameManager).contains(player)) {

            String upperCaseResponse = response.toUpperCase();

            switch (upperCaseResponse) {
                case "YES":
                    MessageSenderService.sendMessage("YouAreContinuingTravel", sender);
                    gameManager.broadcastGameMessageToOthers(new PlayerIsContinuingMessage(player.getName()), sender);

                    gameManager.getGame().getBoard().addTraveler(player);
                    player.setIsReady(true);
                    gameManager.getGameThread().notifyThread();
                    break;

                case "NO":
                    MessageSenderService.sendMessage("YouLeftTravel", sender);
                    gameManager.broadcastGameMessageToOthers(new PlayerLeftMessage(player.getName()), sender);

                    board.leaveTravel(player);

                    player.setIsReady(true);
                    gameManager.getGameThread().notifyThread();
                    break;

                default:
                    MessageSenderService.sendMessage("IncorrectResponse", sender);
                    break;
            }

        } else {
            MessageSenderService.sendMessage("NotAllowedToDoThat", sender);
        }
    }
}