package org.progetto.server.controller;

import org.progetto.messages.toClient.ResponseTrackMessage;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipMessage;
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

        if(!player.getIsReady()){
            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
            gameManager.broadcastGameMessageToOthers(player.getName() + " is ready", sender);
        }

        sender.sendMessage("YouAreReady");
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

        try {
            ArrayList<Player> travelers = gameManager.getGame().getBoard().getCopyTravelers();
            Player[] track = gameManager.getGame().getBoard().getTrack();
            sender.sendMessage(new ResponseTrackMessage(travelers, track));

        }catch (IllegalStateException e) {
            sender.sendMessage(e.getMessage());
        }
    }
}