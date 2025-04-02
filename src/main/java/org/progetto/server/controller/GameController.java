package org.progetto.server.controller;

import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.GamePhase;

import java.rmi.RemoteException;

/**
 * Game controller class
 */
public class GameController {

    // =======================
    // OTHER METHODS
    // =======================

    public static void startGame(GameManager gameManager, Sender sender) throws RemoteException {

        if(gameManager.getGame().getPhase() != GamePhase.INIT){
            sender.sendMessage("GameAlreadyStarted");
            return;
        }

        gameManager.broadcastGameMessage("StartGame");

        gameManager.getGame().setPhase(GamePhase.BUILDING);

        gameManager.startTimer();
    }
}