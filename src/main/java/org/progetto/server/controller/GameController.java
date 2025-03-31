package org.progetto.server.controller;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.*;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameCommunicationHandler;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.events.EventCard;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Game controller class
 */
public class GameController {

    // =======================
    // ATTRIBUTES
    // =======================

    private static final AtomicInteger currentIdGame = new AtomicInteger(0);

    // =======================
    // OTHER METHODS
    // =======================

    public static void startGame(GameCommunicationHandler gameCommunicationHandler, Sender sender) throws RemoteException {

        if(gameCommunicationHandler.getGame().getPhase() != GamePhase.INIT){
            sender.sendMessage("GameAlreadyStarted");
            return;
        }

        gameCommunicationHandler.broadcastGameMessage("StartGame");

        gameCommunicationHandler.getGame().setPhase(GamePhase.BUILDING);

        gameCommunicationHandler.startTimer();
    }
}