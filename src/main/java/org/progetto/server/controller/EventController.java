package org.progetto.server.controller;

import org.progetto.messages.toClient.DiceResultMessage;
import org.progetto.messages.toClient.PickedEventCardMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.Player;
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
     * Handles the request to roll dice
     *
     * @author Gabriele
     * @param gameManager
     * @param player
     * @param sender
     * @throws RemoteException
     */
    public static void rollDice(GameManager gameManager, Player player, Sender sender) throws RemoteException {

        try{
            int result = player.rollDice();
            LobbyController.broadcastLobbyMessage(new DiceResultMessage(result));

        } catch (IllegalStateException e) {
            sender.sendMessage(e.getMessage());
        }
    }

    /**
     * Handles decision to pick an eventCard
     *
     * @author Lorenzo
     * @param gameManager is the class that manage the current game
     * @param sender
     * @throws RemoteException if the eventCard can't be picked
     */
    public static void pickEventCard(GameManager gameManager, Sender sender) throws RemoteException {

        try{
            EventCard card = gameManager.getGame().pickEventCard();
            LobbyController.broadcastLobbyMessage(new PickedEventCardMessage(card.getImgSrc()));

            gameManager.createEventController();

            gameManager.getEventController().start();

        }catch (IllegalStateException e) {
            if(e.getMessage().equals("EmptyHiddenEventCardDeck"))
                sender.sendMessage("EmptyHiddenEventCardDeck");

        }
    }

    /**
     * @author Lorenzo
     */
    public void start() throws RemoteException {
        System.out.println("Unable To Start EventCard");
    }
}