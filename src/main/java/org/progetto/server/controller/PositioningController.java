package org.progetto.server.controller;

import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.Positioning.StartingPositionsMessage;
import org.progetto.messages.toClient.Positioning.AskStartingPositionMessage;
import org.progetto.messages.toClient.Positioning.PlayersInPositioningDecisionOrderMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.Board;
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

        gameManager.broadcastGameMessage(new PlayersInPositioningDecisionOrderMessage(gameManager.getGame().getBoard().getCopyTravelers()));

        Board board = gameManager.getGame().getBoard();

        for (Player player : board.getCopyTravelers()) {

            gameManager.getGame().setActivePlayer(player);
            gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

            Sender sender = gameManager.getSenderByPlayer(player);

            if (sender != null) {
                try {
                    Player[] startingPositions = board.getStartingPositionsCopy();
                    sender.sendMessage(new AskStartingPositionMessage(startingPositions));
                } catch (RemoteException e) {
                    System.err.println("RMI client unreachable");
                }

                gameManager.getGameThread().resetAndWaitTravelerReady(player);

            }else
                insertAtFurthestStartPosition(gameManager, player);
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
            sender.sendMessage("ValidStartingPosition");
            gameManager.broadcastGameMessage(new StartingPositionsMessage(gameManager.getGame().getBoard().getStartingPositionsCopy()));

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

    public static void insertAtFurthestStartPosition(GameManager gameManager, Player player) {
        Board board = gameManager.getGame().getBoard();
        Player[] startingPositions = board.getStartingPositionsCopy();

        for (int i = 0; i < 4; i++) {
            if(startingPositions[i] == null){
                board.decideStartingPositionOnTrack(player, i);
                gameManager.broadcastGameMessage(new StartingPositionsMessage(gameManager.getGame().getBoard().getStartingPositionsCopy()));
                return;
            }
        }
    }

    public static void showStartingPositions(GameManager gameManager, Sender sender) throws RemoteException {
        if (!(gameManager.getGame().getPhase().equals(GamePhase.POSITIONING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        sender.sendMessage(new StartingPositionsMessage(gameManager.getGame().getBoard().getStartingPositionsCopy()));
    }

    /**
     * Shows all players in the positioning decision order (travelers)
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

        sender.sendMessage(new PlayersInPositioningDecisionOrderMessage(players));
    }
}
