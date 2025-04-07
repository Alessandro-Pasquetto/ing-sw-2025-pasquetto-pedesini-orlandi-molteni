package org.progetto.server.controller.events;

import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.SpaceshipController;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.Sabotage;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class SabotageController extends EventControllerAbstract{

    // =======================
    // ATTRIBUTES
    // =======================

    private GameManager gameManager;
    private Sabotage sabotage;
    private String phase;
    private Player penalizedPlayer;
    private ArrayList<Player> activePlayers;
    private int yDiceResult;
    private int xDiceResult;
    private int triesCount;

    // =======================
    // CONSTRUCTORS
    // =======================

    public SabotageController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.sabotage = (Sabotage) gameManager.getGame().getActiveEventCard();
        this.phase = "START";
        this.penalizedPlayer = null;
        this.activePlayers = gameManager.getGame().getBoard().getActivePlayers();
        this.yDiceResult = 0;
        this.xDiceResult = 0;
        this.triesCount = 0;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Starts event card effect
     *
     * @author Gabriele
     * @throws RemoteException
     */
    @Override
    public void start() throws RemoteException {
        if (phase.equals("START")) {
            phase = "LESS_POPULATED";
            lessPopulatedSpaceship();
        }
    }

    /**
     * Finds penalized player and collects him
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void lessPopulatedSpaceship() throws RemoteException {
        if (phase.equals("LESS_POPULATED")) {

            penalizedPlayer = sabotage.lessPopulatedSpaceship(activePlayers);

            gameManager.broadcastGameMessage(new LessPopulatedPlayerMessage(penalizedPlayer.getName()));

            phase = "ASK_ROLL_DICE";
            askToRollDice();
        }
    }

    /**
     * Asks penalized player to roll dice
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void askToRollDice() throws RemoteException {
        if (phase.equals("ASK_ROLL_DICE")) {

            Sender sender = gameManager.getSenderByPlayer(penalizedPlayer);

            if (yDiceResult == 0) {
                sender.sendMessage("ThrowDiceToFindRow");
            } else if (xDiceResult == 0) {
                sender.sendMessage("ThrowDiceToFindColumn");
            }

            phase = "ROLL_DICE";
        }
    }

    /**
     * Rolls dice and collects the result
     *
     * @author Gabriele
     * @param player
     * @param sender
     * @throws RemoteException
     */
    @Override
    public void rollDice(Player player, Sender sender) throws RemoteException {
        if (phase.equals("ROLL_DICE")) {

            // Checks if the player that calls the methods is also the penalty one in the controller
            if (player.equals(penalizedPlayer)) {

                // First dice throw (finds row)
                if (yDiceResult == 0) {
                    yDiceResult = player.rollDice();

                    sender.sendMessage(new DiceResultMessage(yDiceResult));
                    gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiceResultMessage(penalizedPlayer.getName(), yDiceResult), sender);

                    phase = "ASK_ROLL_DICE";
                    askToRollDice();

                // Second dice throw (finds column)
                } else if (xDiceResult == 0) {
                    xDiceResult = player.rollDice();

                    sender.sendMessage(new DiceResultMessage(xDiceResult));
                    gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiceResultMessage(penalizedPlayer.getName(), xDiceResult), sender);

                    phase = "EFFECT";
                    eventEffect(sender);
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Tries to apply event effect to penalized player
     *
     * @author Gabriele
     * @param sender
     * @throws RemoteException
     */
    private void eventEffect(Sender sender) throws RemoteException {
        if (phase.equals("EFFECT")) {

            // Event effect applied for single player
            if (sabotage.penalty(yDiceResult, xDiceResult, penalizedPlayer) != null) {

                // If something got destroyed, sends update message
                // TODO: handle waiting in case of needed decision by player on which part of the ship to hold
                SpaceshipController.destroyComponentAndCheckValidity(gameManager, penalizedPlayer, yDiceResult, xDiceResult, sender);

                // Checks if he lost
                int totalCrew = penalizedPlayer.getSpaceship().getTotalCrewCount();

                if (totalCrew == 0) {
                    gameManager.broadcastGameMessage(new PlayerDefeatedMessage(penalizedPlayer.getName()));
                    gameManager.getGame().getBoard().leaveTravel(penalizedPlayer);
                }

                phase = "END";

            } else {

                gameManager.broadcastGameMessage("NothingDestroyed");

                triesCount++;

                if (triesCount < 3) {
                    // Resets dice results
                    yDiceResult = 0;
                    xDiceResult = 0;

                    phase = "ASK_ROLL_DICE";
                    askToRollDice();

                } else {
                    phase = "END";
                    end(sender);
                }

            }
        }
    }

    /**
     * Send a message of end card to all players
     *
     * @author Gabriele
     * @param sender
     * @throws RemoteException
     */
    private void end(Sender sender) throws RemoteException {
        if (phase.equals("END")) {
            sender.sendMessage("Congratulations You Survived");
        }
    }
}
