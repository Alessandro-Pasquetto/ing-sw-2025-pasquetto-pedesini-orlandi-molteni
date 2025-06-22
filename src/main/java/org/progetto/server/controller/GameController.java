package org.progetto.server.controller;

import org.progetto.messages.toClient.ResponsePlayerStatsMessage;
import org.progetto.messages.toClient.Track.ResponseTrackMessage;
import org.progetto.messages.toClient.ResponsePlayersMessage;
import org.progetto.messages.toClient.WaitingPlayersMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;

import java.util.ArrayList;


public class GameController {

    // =======================
    // GETTERS
    // =======================

    /**
     * Returns a list of all the connected players that haven't left the game
     *
     * @author Alessandro
     * @param gameManager is the current gameManager
     * @return the list of connected players
     */
    public static ArrayList<Player> getConnectedTravelers(GameManager gameManager) {
        return new ArrayList<>(gameManager.getGame().getPlayersCopy()
                .stream()
                .filter(player -> !player.getHasLeft())
                .toList());
    }

    /**
     * Returns a list of all the disconnected players that haven't left the game
     *
     * @author Alessandro
     * @param gameManager is the current gameManager
     * @return the list of disconnected players
     */
    public static ArrayList<Player> getDisconnectedTravelers(GameManager gameManager) {
        return new ArrayList<>(gameManager.getDisconnectedPlayersCopy()
                .stream()
                .filter(player -> !player.getHasLeft())
                .toList());
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Enable building phase
     *
     * @author Alessandro
     * @param gameManager current gameManager
     */
    public static void startBuilding(GameManager gameManager) {
        if(gameManager.getGame().getLevel() != 1)
            gameManager.startTimer();
    }

    /**
     * Lets a player decide to be in a ready state
     *
     * @author Alessandro
     * @param gameManager is the current gameManager
     * @param player is the player that wants to be ready
     * @param sender is the current player sender
     */
    public static void ready(GameManager gameManager, Player player, Sender sender) {
        if (!(gameManager.getGame().getPhase().equals(GamePhase.INIT)) && !(gameManager.getGame().getPhase().equals(GamePhase.POPULATING))) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        MessageSenderService.sendMessage("YouAreReady", sender);

        if (!player.getIsReady()) {
            player.setIsReady(true);
            gameManager.getGameThread().notifyThread();
            gameManager.broadcastGameMessageToOthers(player.getName() + " is ready", sender);

            if (gameManager.getGame().getPhase().equals(GamePhase.INIT))
                gameManager.broadcastGameMessage(new WaitingPlayersMessage(gameManager.getGame().getPlayersCopy()));
        }
    }

    /**
     * Handles player decision to show his current stats
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @param sender current sender
     */
    public static void playerStats(GameManager gameManager, Player player, Sender sender){
        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING)) && !(gameManager.getGame().getPhase().equals(GamePhase.ADJUSTING)) && !(gameManager.getGame().getPhase().equals(GamePhase.POPULATING)) && !(gameManager.getGame().getPhase().equals(GamePhase.EVENT)) && !(gameManager.getGame().getPhase().equals(GamePhase.TRAVEL))) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        try {
            String name = player.getName();
            int credits = player.getCredits();
            int position = player.getPosition();
            boolean hasLeft = player.getHasLeft();
            MessageSenderService.sendMessage(new ResponsePlayerStatsMessage(name, credits, position, hasLeft), sender);

        } catch (IllegalStateException e) {
            MessageSenderService.sendMessage(e.getMessage(), sender);
        }
    }

    /**
     * Allows a client to obtain all the active players
     *
     * @author Lorenzo
     * @param gameManager current gameManager
     * @param sender current sender
     */
    public static void showPlayers(GameManager gameManager, Sender sender) {
        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING)) && !(gameManager.getGame().getPhase().equals(GamePhase.ADJUSTING)) && !(gameManager.getGame().getPhase().equals(GamePhase.POPULATING)) && !(gameManager.getGame().getPhase().equals(GamePhase.EVENT)) && !(gameManager.getGame().getPhase().equals(GamePhase.TRAVEL))) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }
        try {
            MessageSenderService.sendMessage(new ResponsePlayersMessage(gameManager.getGame().getPlayersCopy()), sender);
        } catch (IllegalStateException e) {
            MessageSenderService.sendMessage(e.getMessage(), sender);
        }
    }

    /**
     * Handles player decision to show current track
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @param sender current sender
     */
    public static void showTrack(GameManager gameManager, Sender sender) {
        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING)) && !(gameManager.getGame().getPhase().equals(GamePhase.EVENT)) && !(gameManager.getGame().getPhase().equals(GamePhase.TRAVEL))) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        try {
            ArrayList<Player> travelers = gameManager.getGame().getBoard().getCopyTravelers();
            Player[] track = gameManager.getGame().getBoard().getTrack();
            MessageSenderService.sendMessage(new ResponseTrackMessage(travelers, track), sender);

        }catch (IllegalStateException e) {
            MessageSenderService.sendMessage(e.getMessage(), sender);
        }
    }


    /**
     * Return an ArrayList of the players in the track as a copy of the original
     *
     * @author Alessandro
     * @param gameManager is the current gameManager
     * @return the player list
     */
    public static ArrayList<Player> getAllPlayersInTrackCopy(GameManager gameManager) {
        ArrayList<Player> playersInTrack = new ArrayList<>();

        playersInTrack.addAll(
                gameManager.getGame().getPlayersCopy()
                        .stream()
                        .filter(player -> !player.getHasLeft())
                        .toList()
        );

        playersInTrack.addAll(
                gameManager.getDisconnectedPlayersCopy()
                        .stream()
                        .filter(player -> !player.getHasLeft())
                        .toList()
        );

        return playersInTrack;
    }

    /**
     * Allows to obtain the ready state of all the connected travellers
     *
     * @author Alessandro
     * @param gameManager is the current gameManager
     * @return true if all the players are ready
     */
    public static boolean allConnectedTravelersReady(GameManager gameManager) {
        for (Player traveler : getConnectedTravelers(gameManager)) {
            if(!traveler.getIsReady())
                return false;
        }

        return true;
    }

    /**
     * Allows to obtain the list of connected player and checks if all the players are ready
     *
     * @author Alessandro
     * @param players are all the players in game
     * @param gameManager is the current gameManager
     * @return true if all the connected players are ready
     */
    public static boolean allConnectedParametersPlayersReady(ArrayList<Player> players, GameManager gameManager) {
        ArrayList<Player> connectedPlayers = new ArrayList<>(players
                .stream()
                .filter(player -> !gameManager.getDisconnectedPlayersCopy().contains(player))
                .toList());;

        for (Player player : connectedPlayers) {
            if(!player.getIsReady())
                return false;
        }

        return true;
    }
}