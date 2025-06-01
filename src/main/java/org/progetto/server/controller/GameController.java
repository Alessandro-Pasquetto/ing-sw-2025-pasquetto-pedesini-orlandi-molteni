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

/**
 * Game controller class
 */
public class GameController {

    // =======================
    // OTHER METHODS
    // =======================

    public static void startBuilding(GameManager gameManager) {
        if(gameManager.getGame().getLevel() != 1)
            gameManager.startTimer();
    }

    public static void ready(GameManager gameManager, Player player, Sender sender) {
        if (!(gameManager.getGame().getPhase().equals(GamePhase.INIT)) && !(gameManager.getGame().getPhase().equals(GamePhase.POPULATING))) {
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        MessageSenderService.sendOptional("YouAreReady", sender);

        if(!player.getIsReady()){
            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
            gameManager.broadcastGameMessageToOthers(player.getName() + " is ready", sender);

            if(gameManager.getGame().getPhase().equals(GamePhase.INIT))
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        try {
            String name = player.getName();
            int credits = player.getCredits();
            int position = player.getPosition();
            boolean hasLeft = player.getHasLeft();
            MessageSenderService.sendOptional(new ResponsePlayerStatsMessage(name, credits, position, hasLeft), sender);

        }catch (IllegalStateException e) {
            MessageSenderService.sendOptional(e.getMessage(), sender);
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }
        try {
            MessageSenderService.sendOptional(new ResponsePlayersMessage(gameManager.getGame().getPlayersCopy()), sender);
        }catch (IllegalStateException e) {
            MessageSenderService.sendOptional(e.getMessage(), sender);
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        try {
            ArrayList<Player> travelers = gameManager.getGame().getBoard().getCopyTravelers();
            Player[] track = gameManager.getGame().getBoard().getTrack();
            MessageSenderService.sendOptional(new ResponseTrackMessage(travelers, track), sender);

        }catch (IllegalStateException e) {
            MessageSenderService.sendOptional(e.getMessage(), sender);
        }
    }

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

    public static ArrayList<Player> getConnectedTravelers(GameManager gameManager) {

        return new ArrayList<>(gameManager.getGame().getPlayersCopy()
                .stream()
                .filter(player -> !player.getHasLeft())
                .toList());
    }

    public static ArrayList<Player> getDisconnectedTravelers(GameManager gameManager) {
        return new ArrayList<>(gameManager.getDisconnectedPlayersCopy()
                .stream()
                .filter(player -> !player.getHasLeft())
                .toList());
    }

    public static boolean allConnectedTravelersReady(GameManager gameManager) {
        for (Player traveler : getConnectedTravelers(gameManager)) {
            if(!traveler.getIsReady())
                return false;
        }

        return true;
    }
}