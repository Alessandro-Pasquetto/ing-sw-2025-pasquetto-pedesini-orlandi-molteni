package org.progetto.server.controller;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.DiceResultMessage;
import org.progetto.messages.toClient.PickedComponentMessage;
import org.progetto.messages.toClient.PickedEventCardMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameCommunicationHandler;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
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
     * @param gameCommunicationHandler
     * @param player
     * @param sender
     * @throws RemoteException
     */
    public static void rollDice(GameCommunicationHandler gameCommunicationHandler, Player player, Sender sender) throws RemoteException {

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
     * @param gameCommunicationHandler is the class that manage the current game
     * @param sender
     * @throws RemoteException if the eventCard can't be picked
     */
    public static void pickEventCard(GameCommunicationHandler gameCommunicationHandler, Sender sender) throws RemoteException {

        try{
            EventCard card = gameCommunicationHandler.getGame().pickEventCard();
            LobbyController.broadcastLobbyMessage(new PickedEventCardMessage(card.getImgSrc()));

            gameCommunicationHandler.createEventController();



        }catch (IllegalStateException e) {
            if(e.getMessage().equals("EmptyHiddenEventCardDeck"))
                sender.sendMessage("EmptyHiddenEventCardDeck");

        }

    }
}