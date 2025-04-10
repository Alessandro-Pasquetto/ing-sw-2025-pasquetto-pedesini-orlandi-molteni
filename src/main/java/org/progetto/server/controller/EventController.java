package org.progetto.server.controller;

import org.progetto.messages.toClient.Building.PickedEventCardMessage;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.events.EventCard;

import java.rmi.RemoteException;

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

        System.out.println(card.getType().toString());
        gameManager.broadcastGameMessage(new PickedEventCardMessage(card));

        gameManager.getGameThread().notifyThread();
    }
}