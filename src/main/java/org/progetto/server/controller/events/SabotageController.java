package org.progetto.server.controller.events;

import org.progetto.messages.toClient.EventCommon.AnotherPlayerDiceResultMessage;
import org.progetto.messages.toClient.EventCommon.DiceResultMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.messages.toClient.Sabotage.LessPopulatedPlayerMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.controller.SpaceshipController;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.Sabotage;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class SabotageController extends EventControllerAbstract{

    // =======================
    // ATTRIBUTES
    // =======================

    private final Sabotage sabotage;
    private Player penalizedPlayer;
    private int yDiceResult;
    private int xDiceResult;
    private int triesCount;

    // =======================
    // CONSTRUCTORS
    // =======================

    public SabotageController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.sabotage = (Sabotage) gameManager.getGame().getActiveEventCard();
        this.phase = EventPhase.START;
        this.penalizedPlayer = null;
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
        if (!phase.equals(EventPhase.START))
            throw new IllegalStateException("IncorrectPhase");

        phase = EventPhase.LESS_POPULATED;
        lessPopulatedSpaceship();
    }

    /**
     * Finds penalized player and collects him
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void lessPopulatedSpaceship() throws RemoteException {
        if (!phase.equals(EventPhase.LESS_POPULATED))
            throw new IllegalStateException("IncorrectPhase");

        ArrayList<Player> activePlayers = gameManager.getGame().getBoard().getCopyTravelers();

        penalizedPlayer = sabotage.lessPopulatedSpaceship(activePlayers);

        gameManager.broadcastGameMessage(new LessPopulatedPlayerMessage(penalizedPlayer.getName()));

        phase = EventPhase.ASK_ROLL_DICE;
        askToRollDice();
    }

    /**
     * Asks penalized player to roll dice
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void askToRollDice() throws RemoteException {
        if (!phase.equals(EventPhase.ASK_ROLL_DICE))
            throw new IllegalStateException("IncorrectPhase");

        Sender sender = gameManager.getSenderByPlayer(penalizedPlayer);

        if (yDiceResult == 0) {
            sender.sendMessage("ThrowDiceToFindRow");
        } else if (xDiceResult == 0) {
            sender.sendMessage("ThrowDiceToFindColumn");
        }

        phase = EventPhase.ROLL_DICE;
    }

    /**
     * Rolls dice and collects the result
     *
     * @author Gabriele
     * @param player current player
     * @param sender current sender
     * @throws RemoteException
     */
    @Override
    public void rollDice(Player player, Sender sender) throws RemoteException {
        if (!phase.equals(EventPhase.ROLL_DICE))
            sender.sendMessage("IncorrectPhase");

        // Checks if the player that calls the methods is also the penalty one in the controller
        if (!player.equals(penalizedPlayer))
            sender.sendMessage("NotYourTurn");

        // First dice throw (finds row)
        if (yDiceResult == 0) {
            yDiceResult = player.rollDice();

            sender.sendMessage(new DiceResultMessage(yDiceResult));
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiceResultMessage(penalizedPlayer.getName(), yDiceResult), sender);

            phase = EventPhase.ASK_ROLL_DICE;
            askToRollDice();

        // Second dice throw (finds column)
        } else if (xDiceResult == 0) {
            xDiceResult = player.rollDice();

            sender.sendMessage(new DiceResultMessage(xDiceResult));
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiceResultMessage(penalizedPlayer.getName(), xDiceResult), sender);

            phase = EventPhase.EFFECT;
            eventEffect(sender);
        }
    }

    /**
     * Tries to apply event effect to penalized player
     *
     * @author Gabriele
     * @param sender current sender
     * @throws RemoteException
     */
    private void eventEffect(Sender sender) throws RemoteException {
        if (!phase.equals(EventPhase.EFFECT))
            throw new IllegalStateException("IncorrectPhase");

        // Event effect applied for single player
        if (sabotage.penalty(yDiceResult, xDiceResult, penalizedPlayer) != null) {

            // If something got destroyed, sends update message
            // TODO: handle waiting in case of needed decision by player on which part of the ship to hold
            SpaceshipController.destroyComponentAndCheckValidity(gameManager, penalizedPlayer, xDiceResult, yDiceResult, sender);

            // Checks if he lost
            int totalCrew = penalizedPlayer.getSpaceship().getTotalCrewCount();

            if (totalCrew == 0) {
                gameManager.broadcastGameMessage(new PlayerDefeatedMessage(penalizedPlayer.getName()));
                gameManager.getGame().getBoard().leaveTravel(penalizedPlayer);
            }

            phase = EventPhase.END;

        } else {

            gameManager.broadcastGameMessage("NothingDestroyed");

            triesCount++;

            if (triesCount < 3) {
                // Resets dice results
                yDiceResult = 0;
                xDiceResult = 0;

                phase = EventPhase.ASK_ROLL_DICE;
                askToRollDice();

            } else {
                phase = EventPhase.END;
                end(sender);
            }
        }
    }

    /**
     * Send a message of end card to all players
     *
     * @author Gabriele
     * @param sender current sender
     * @throws RemoteException
     */
    private void end(Sender sender) throws RemoteException {
        if (!phase.equals(EventPhase.END))
            throw new IllegalStateException("IncorrectPhase");

        sender.sendMessage("Congratulations You Survived");
    }
}
