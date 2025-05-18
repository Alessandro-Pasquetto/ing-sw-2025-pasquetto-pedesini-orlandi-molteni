package org.progetto.server.controller;

import org.progetto.messages.toClient.AnotherPlayerIsActiveMessage;
import org.progetto.messages.toClient.Positioning.AnotherPlayerSetStartingPositionMessage;
import org.progetto.messages.toClient.Positioning.AskStartingPositionMessage;
import org.progetto.messages.toClient.Positioning.PlayerSetStartingPositionMessage;
import org.progetto.messages.toClient.Positioning.ShowPlayersInPositioningDecisionOrderMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Positioning phase controller class
 */
public class PositioningController {

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Ask for starting position to all players
     *
     * @author Gabriele
     * @param gameManager current gameManager
     */
    public static void askForStartingPosition(GameManager gameManager) throws InterruptedException, RemoteException {

        gameManager.broadcastGameMessage(new ShowPlayersInPositioningDecisionOrderMessage(gameManager.getGame().getBoard().getCopyTravelers()));

        for (Player player : gameManager.getGame().getBoard().getCopyTravelers()) {
            Sender sender = gameManager.getSenderByPlayer(player);
            if (sender != null) {
                try {
                    Player[] startingPositions = gameManager.getGame().getBoard().getStartingPositions();
                    sender.sendMessage(new AskStartingPositionMessage(startingPositions));
                } catch (RemoteException e) {
                    System.err.println("RMI client unreachable");
                }
            }

            gameManager.getGame().setActivePlayer(player);
            sender.sendMessage("YourTurn");
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerIsActiveMessage(player.getName()), sender);

            gameManager.getGameThread().resetAndWaitPlayerReady(player);
        }
    }

    /**
     * Handles player decision to set starting position
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @param player current player
     * @param startingPosition the starting position of the player
     * @param sender current sender
     * @throws RemoteException
     */
    public static void receiveStartingPosition(GameManager gameManager, Player player, int startingPosition, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.POSITIONING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            sender.sendMessage("NotYourTurn");
            return;
        }

        try {
            gameManager.getGame().getBoard().decideStartingPositionOnTrack(player, startingPosition);
            sender.sendMessage(new PlayerSetStartingPositionMessage(gameManager.getGame().getBoard().getStartingPositions()));
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerSetStartingPositionMessage(player.getName(), gameManager.getGame().getBoard().getStartingPositions()), sender);

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } catch (IllegalStateException e) {
            if (e.getMessage().equals("StartingPositionAlreadyTaken"))
                sender.sendMessage("StartingPositionAlreadyTaken");
            else if (e.getMessage().equals("InvalidStartingPosition"))
                sender.sendMessage("InvalidStartingPosition");
            else if (e.getMessage().equals("PlayerAlreadyHasAStartingPosition"))
                sender.sendMessage("PlayerAlreadyHasAStartingPosition");
            else
                sender.sendMessage(e.getMessage());
        }
    }

    /**
     * Shows all players in the positioning decision order
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @param sender current sender
     * @throws RemoteException
     */
    public static void showPlayersInPositioningDecisionOrder(GameManager gameManager, Sender sender) throws RemoteException {
        if (!(gameManager.getGame().getPhase().equals(GamePhase.POSITIONING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        ArrayList<Player> players = gameManager.getGame().getBoard().getCopyTravelers();

        sender.sendMessage(new ShowPlayersInPositioningDecisionOrderMessage(players));
    }
}
