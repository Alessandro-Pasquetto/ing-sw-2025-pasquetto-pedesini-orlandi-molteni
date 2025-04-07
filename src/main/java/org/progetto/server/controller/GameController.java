package org.progetto.server.controller;

import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;

import java.rmi.RemoteException;

/**
 * Game controller class
 */
public class GameController {

    // =======================
    // OTHER METHODS
    // =======================

    public static void startBuilding(GameManager gameManager) throws RemoteException {

        gameManager.broadcastGameMessage("StartBuilding");

        gameManager.getGame().setPhase(GamePhase.BUILDING);

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
}