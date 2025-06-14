package org.progetto.server.controller;

import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.EventGeneric.AvailableBoxesMessage;
import org.progetto.messages.toClient.Positioning.StartingPositionsMessage;
import org.progetto.messages.toClient.Positioning.AskStartingPositionMessage;
import org.progetto.messages.toClient.Positioning.PlayersInPositioningDecisionOrderMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.Board;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;

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
    public static void askForStartingPosition(GameManager gameManager) throws InterruptedException {

        gameManager.broadcastGameMessage(new PlayersInPositioningDecisionOrderMessage(gameManager.getGame().getBoard().getCopyTravelers()));

        Board board = gameManager.getGame().getBoard();

        for (Player player : board.getCopyTravelers()) {

            gameManager.getGame().setActivePlayer(player);
            gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

            Sender sender = gameManager.getSenderByPlayer(player);

            Player[] startingPositions = board.getStartingPositionsCopy();
            MessageSenderService.sendMessage(new AskStartingPositionMessage(startingPositions), sender);

            gameManager.getGameThread().resetAndWaitTravelerReady(player);

            // If the player is disconnected
            if(!player.getIsReady())
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
     */
    public static void receiveStartingPosition(GameManager gameManager, Player player, int startingPosition, Sender sender){

        if (!(gameManager.getGame().getPhase().equals(GamePhase.POSITIONING))) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        try {
            gameManager.getGame().getBoard().decideStartingPositionOnTrack(player, startingPosition);
            MessageSenderService.sendMessage("ValidStartingPosition", sender);
            gameManager.broadcastGameMessage(new StartingPositionsMessage(gameManager.getGame().getBoard().getStartingPositionsCopy()));

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } catch (IllegalStateException e) {
            if (e.getMessage().equals("StartingPositionAlreadyTaken"))
                MessageSenderService.sendMessage("StartingPositionAlreadyTaken", sender);
            else if (e.getMessage().equals("InvalidStartingPosition"))
                MessageSenderService.sendMessage("InvalidStartingPosition", sender);
            else if (e.getMessage().equals("PlayerAlreadyHasAStartingPosition"))
                MessageSenderService.sendMessage("PlayerAlreadyHasAStartingPosition", sender);
            else
                MessageSenderService.sendMessage(e.getMessage(), sender);
        }
    }


    /**
     * Asks a player for starting position
     *
     * @author Alessandro
     * @param gameManager is the current GameManager
     * @param player is the player that needs to decide
     */
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

    /**
     * Allows to show all the starting position on the track
     *
     * @author Alessandro
     * @param gameManager is the current GameManager
     * @param sender is the current Sender
     */
    public static void showStartingPositions(GameManager gameManager, Sender sender){
        if (!(gameManager.getGame().getPhase().equals(GamePhase.POSITIONING))) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        MessageSenderService.sendMessage(new StartingPositionsMessage(gameManager.getGame().getBoard().getStartingPositionsCopy()), sender);
    }

    /**
     * Shows all players in the positioning decision order (travelers)
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @param sender current sender
     */
    public static void showPlayersInPositioningDecisionOrder(GameManager gameManager, Sender sender){
        if (!(gameManager.getGame().getPhase().equals(GamePhase.POSITIONING))) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        ArrayList<Player> players = gameManager.getGame().getBoard().getCopyTravelers();

        MessageSenderService.sendMessage(new PlayersInPositioningDecisionOrderMessage(players), sender);
    }

    public static void reconnectPlayer(GameManager gameManager, Player player, Sender sender) {
        if(!player.equals(gameManager.getGame().getActivePlayer()))
            return;

        Player[] startingPositions = gameManager.getGame().getBoard().getStartingPositionsCopy();
        MessageSenderService.sendMessage(new AskStartingPositionMessage(startingPositions), sender);
    }
}
