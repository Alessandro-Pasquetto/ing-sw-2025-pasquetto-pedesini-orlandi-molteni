package org.progetto.server.controller;

import org.progetto.messages.toClient.PlayerStatsMessage;
import org.progetto.messages.toClient.TrackMessage;
import org.progetto.messages.toClient.PlayersMessage;
import org.progetto.messages.toClient.WaitingPlayersMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Game controller class
 */
public class GameController {

    // =======================
    // OTHER METHODS
    // =======================

    public static void startBuilding(GameManager gameManager) throws RemoteException {
        if(gameManager.getGame().getLevel() != 1)
            gameManager.startTimer();
    }

    public static void ready(GameManager gameManager, Player player, Sender sender) throws RemoteException {
        if (!(gameManager.getGame().getPhase().equals(GamePhase.INIT)) && !(gameManager.getGame().getPhase().equals(GamePhase.POPULATING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        sender.sendMessage("YouAreReady");

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
     * @throws RemoteException
     */
    public static void playerStats(GameManager gameManager, Player player, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING)) && !(gameManager.getGame().getPhase().equals(GamePhase.ADJUSTING)) && !(gameManager.getGame().getPhase().equals(GamePhase.POPULATING)) && !(gameManager.getGame().getPhase().equals(GamePhase.EVENT)) && !(gameManager.getGame().getPhase().equals(GamePhase.TRAVEL))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        try {
            String name = player.getName();
            int credits = player.getCredits();
            int position = player.getPosition();
            boolean hasLeft = player.getHasLeft();
            sender.sendMessage(new PlayerStatsMessage(name, credits, position, hasLeft));

        }catch (IllegalStateException e) {
            sender.sendMessage(e.getMessage());
        }
    }


    /**
     * Allows a client to obtain all the active players
     *
     * @author Lorenzo
     * @param gameManager current gameManager
     * @param sender current sender
     * @throws RemoteException
     */
    public static void showPlayers(GameManager gameManager, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING)) && !(gameManager.getGame().getPhase().equals(GamePhase.ADJUSTING)) && !(gameManager.getGame().getPhase().equals(GamePhase.POPULATING)) && !(gameManager.getGame().getPhase().equals(GamePhase.EVENT)) && !(gameManager.getGame().getPhase().equals(GamePhase.TRAVEL))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }
        try {
            sender.sendMessage(new PlayersMessage(gameManager.getGame().getPlayersCopy()));
        }catch (IllegalStateException e) {
            sender.sendMessage(e.getMessage());
        }
    }

    /**
     * Handles player decision to show current track
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @param sender current sender
     * @throws RemoteException
     */
    public static void showTrack(GameManager gameManager, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING)) && !(gameManager.getGame().getPhase().equals(GamePhase.EVENT)) && !(gameManager.getGame().getPhase().equals(GamePhase.TRAVEL))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        try {
            ArrayList<Player> travelers = gameManager.getGame().getBoard().getCopyTravelers();
            Player[] track = gameManager.getGame().getBoard().getTrack();
            sender.sendMessage(new TrackMessage(travelers, track));

        }catch (IllegalStateException e) {
            sender.sendMessage(e.getMessage());
        }
    }

    public static void removeDisconnectedPlayersFromTravelers(GameManager gameManager){

        for(Player player : gameManager.getDisconnectedPlayersCopy()){

            gameManager.getGame().getBoard().removeTraveler(player);
        }
    }
}