package org.progetto.server.controller;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.DiceResultMessage;
import org.progetto.messages.toClient.PickedComponentMessage;
import org.progetto.server.connection.games.GameCommunicationHandler;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;

/**
 * Event phase controller class
 */
public class EventController {

    // =======================
    // OTHER METHODS
    // =======================

    public static void rollDice(GameCommunicationHandler gameCommunicationHandler, Player player, SocketWriter swSender, VirtualClient vvSender) {

        try{
            int result = player.rollDice();
            LobbyController.broadcastLobbyMessage(new DiceResultMessage(result));

        } catch (IllegalStateException e) {
            GameController.sendMessage(e.getMessage(), swSender, vvSender);
        }
    }
}