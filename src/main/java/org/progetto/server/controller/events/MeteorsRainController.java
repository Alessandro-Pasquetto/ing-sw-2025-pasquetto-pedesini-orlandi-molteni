package org.progetto.server.controller.events;

import org.progetto.messages.toClient.AffectedComponentMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.controller.SpaceshipController;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.events.MeteorsRain;
import org.progetto.server.model.events.Projectile;
import org.progetto.server.model.events.ProjectileSize;

import java.util.ArrayList;

public class MeteorsRainController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private final MeteorsRain meteorsRain;
    private ArrayList<Player> activePlayers;
    private int diceResult;
    private Projectile comingMeteor;
    private final ArrayList<Player> decisionPlayers;
    private final ArrayList<Player> discardedBattery;

    // =======================
    // CONSTRUCTORS
    // =======================

    public MeteorsRainController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.meteorsRain = (MeteorsRain) gameManager.getGame().getActiveEventCard();
        this.phase = EventPhase.START;
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();
        this.diceResult = 0;
        this.decisionPlayers = new ArrayList<>();
        this.discardedBattery = new ArrayList<>();
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Starts event card effect
     *
     * @author Gabriele
     */
    @Override
    public void start() throws InterruptedException {
        if (!phase.equals(EventPhase.START))
            throw new IllegalStateException("IncorrectPhase");

        gameManager.broadcastGameMessage("ResetActivePlayer");

        phase = EventPhase.SEND_METEOR;
        sendMeteor();
    }

    /**
     * Send broadcast the incoming meteor information
     *
     * @author Gabriele
     */
    private void sendMeteor() throws InterruptedException {
        if (!phase.equals(EventPhase.SEND_METEOR))
            throw new IllegalStateException("IncorrectPhase");

        ArrayList<Projectile> meteors = meteorsRain.getMeteors();
        activePlayers = gameManager.getGame().getBoard().getCopyTravelers();

        for (Projectile meteor : meteors) {
            comingMeteor = meteor;

            // Sends to each player information about incoming meteor
            for (Player player : activePlayers) {
                Sender sender = gameManager.getSenderByPlayer(player);

                MessageSenderService.sendOptional(new IncomingProjectileMessage(meteor), sender);
            }

            phase = EventPhase.ASK_ROLL_DICE;
            askToRollDice();

            // Resets elaboration attributes
            decisionPlayers.clear();
            discardedBattery.clear();
        }
    }

    /**
     * Asks the leader to trow the dices
     *
     * @author Gabriele
     */
    private void askToRollDice() throws InterruptedException {
        if (!phase.equals(EventPhase.ASK_ROLL_DICE))
            throw new IllegalStateException("IncorrectPhase");

        Player activePlayer = activePlayers.getFirst();

        Sender sender = gameManager.getSenderByPlayer(activePlayer);

        phase = EventPhase.ROLL_DICE;
        try{
            if (comingMeteor.getFrom() == 0 || comingMeteor.getFrom() == 2) {
                MessageSenderService.sendCritical("RollDiceToFindColumn", sender);
            }

            else if (comingMeteor.getFrom() == 1 || comingMeteor.getFrom() == 3) {
                MessageSenderService.sendCritical("RollDiceToFindRow", sender);
            }

            gameManager.getGameThread().resetAndWaitTravelersReady();

            // If the player is disconnected
            if(!activePlayer.getIsReady()){
                rollDice(activePlayer, sender);
                gameManager.getGameThread().resetAndWaitTravelersReady();
            }

        } catch (Exception e) {
            rollDice(activePlayer, sender);
            gameManager.getGameThread().resetAndWaitTravelersReady();
        }
    }

    /**
     * Rolls dice and collects the result
     *
     * @author Gabriele
     * @param player is the one that needs to trow the dices
     * @param sender current sender
     */
    @Override
    public void rollDice(Player player, Sender sender) {
        if (!phase.equals(EventPhase.ROLL_DICE)) {
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls method is the leader
        if (!player.equals(activePlayers.getFirst())) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
            return;
        }

        diceResult = player.rollDice();

        MessageSenderService.sendOptional(new DiceResultMessage(diceResult), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiceResultMessage(activePlayers.getFirst().getName(), diceResult), sender);

        // Delay to show the dice result
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (comingMeteor.getSize().equals(ProjectileSize.SMALL)) {
            phase = EventPhase.HANDLE_SMALL_METEOR;
            handleSmallMeteor();
        } else {
            phase = EventPhase.HANDLE_BIG_METEOR;
            handleBigMeteor();
        }
    }

    /**
     * Handles current small meteor for each player
     *
     * @author Gabriele
     */
    private void handleSmallMeteor() {
        if (!phase.equals(EventPhase.HANDLE_SMALL_METEOR))
            throw new IllegalStateException("IncorrectPhase");

        // For each player checks impact point
        for (Player player : activePlayers) {

            // Retrieves current player sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            // Finds impact component
            Component affectedComponent = meteorsRain.checkImpactComponent(gameManager.getGame(), player, comingMeteor, diceResult);

            // Checks if there is any affected component
            if (affectedComponent == null) {
                MessageSenderService.sendOptional("NoComponentHit", sender);

                player.setIsReady(true, gameManager.getGame());
                continue;
            }

            // Checks if component have any exposed connector in shot's direction
            if (affectedComponent.getConnections()[comingMeteor.getFrom()] == 0) {
                MessageSenderService.sendOptional("NoComponentDamaged", sender);

                player.setIsReady(true, gameManager.getGame());
                continue;
            }

            // Checks for shields in that direction and at least a battery
            if (meteorsRain.checkShields(player, comingMeteor) && player.getSpaceship().getBatteriesCount() > 0) {
                decisionPlayers.add(player);
                MessageSenderService.sendOptional(new AffectedComponentMessage(affectedComponent.getX(), affectedComponent.getY()), sender);

            // Notifies component destruction
            } else {
                MessageSenderService.sendOptional("NoShieldAvailable", sender);
                handleCurrentMeteor(player);
            }
        }

        // Checks if there is at least one player that can decide to use a shield
        if (!decisionPlayers.isEmpty()) {
            phase = EventPhase.ASK_TO_PROTECT;
            askToProtect();
            return;
        }

        gameManager.getGameThread().notifyThread();
    }

    /**
     * Handles current small meteor for each player
     *
     * @author Gabriele
     */
    private void handleBigMeteor() {
        if (!phase.equals(EventPhase.HANDLE_BIG_METEOR))
            throw new IllegalStateException("IncorrectPhase");

        // For each player checks impact point
        for (Player player : activePlayers) {

            // Retrieves current player sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            // Finds impact component
            Component affectedComponent = meteorsRain.checkImpactComponent(gameManager.getGame(), player, comingMeteor, diceResult);

            // Checks if there is any affected component
            if (affectedComponent == null) {
                MessageSenderService.sendOptional("NoComponentHit", sender);

                player.setIsReady(true, gameManager.getGame());
                continue;
            }

            // Checks if component is a cannon positioned in the same direction as the meteor
            if (affectedComponent.getType().equals(ComponentType.CANNON) && affectedComponent.getRotation() == comingMeteor.getFrom()) {
                MessageSenderService.sendOptional("MeteorDestroyed", sender);

                player.setIsReady(true, gameManager.getGame());
                continue;
            }

            // Checks if component is a double cannon positioned in the same direction as the meteor, and at least a battery
            if (affectedComponent.getType().equals(ComponentType.DOUBLE_CANNON) && affectedComponent.getRotation() == comingMeteor.getFrom() && player.getSpaceship().getBatteriesCount() > 0) {
                decisionPlayers.add(player);
                MessageSenderService.sendOptional(new AffectedComponentMessage(affectedComponent.getX(), affectedComponent.getY()), sender);

            // Notifies component destruction
            } else {
                MessageSenderService.sendOptional("NoCannonAvailable", sender);
                handleCurrentMeteor(player);
            }
        }

        // Checks if there is at least one player that can decide to use a double cannon
        if (!decisionPlayers.isEmpty()) {
            phase = EventPhase.ASK_TO_PROTECT;
            askToProtect();
            return;
        }

        gameManager.getGameThread().notifyThread();
    }

    /**
     * Asks players if they want to protect (shield or double cannon)
     *
     * @author Gabriele
     */
    private void askToProtect() {
        if (!phase.equals(EventPhase.ASK_TO_PROTECT))
            throw new IllegalStateException("IncorrectPhase");

        for (Player player : decisionPlayers) {

            // Asks current player if he wants to use a protection
            Sender sender = gameManager.getSenderByPlayer(player);

            if (comingMeteor.getSize().equals(ProjectileSize.SMALL))
                MessageSenderService.sendOptional("AskToUseShield", sender);
            else
                MessageSenderService.sendOptional("AskToUseDoubleCannon", sender);

            phase = EventPhase.PROTECTION_DECISION;
        }
    }

    /**
     * Receives player decision about the usage of shields or double cannon
     *
     * @author Gabriele
     * @param player is the one that send the decision
     * @param response is the given decision
     * @param sender current sender
     */
    @Override
    public synchronized void receiveProtectionDecision(Player player, String response, Sender sender) {

        // Checks if it is not part of non-protected player, and it is not already contained in protected one list
        if (!decisionPlayers.contains(player)) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
            return;
        }

        String upperCaseResponse = response.toUpperCase();

        switch (upperCaseResponse) {
            case "YES":
                discardedBattery.add(player);
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(1), sender);
                break;

            case "NO":
                handleCurrentMeteor(player);
                break;

            default:
                MessageSenderService.sendOptional("IncorrectResponse", sender);
                break;
        }
    }

    /**
     * Receives the coordinates of BatteryStorage component from which remove a battery to activate protection
     *
     * @author Gabriele
     * @param player that send the decision about the battery position
     * @param xBatteryStorage coordinate of the storage
     * @param yBatteryStorage coordinate of the storage
     * @param sender current sender
     */
    @Override
    public synchronized void receiveDiscardedBatteries(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) {

        // Checks if the player that calls the methods has to discard a battery to activate protection
        if (!discardedBattery.contains(player)) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xBatteryStorage < 0 || yBatteryStorage < 0 || yBatteryStorage >= spaceshipMatrix.length || xBatteryStorage >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendOptional("InvalidCoordinates", sender);
            MessageSenderService.sendOptional(new BatteriesToDiscardMessage(1), sender);
            return;
        }

        Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

        // Checks if component is a battery storage
        if (batteryStorage == null || !batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {
            MessageSenderService.sendOptional("InvalidComponent", sender);
            MessageSenderService.sendOptional(new BatteriesToDiscardMessage(1), sender);
            return;
        }

        // Checks if a battery has been discarded
        if (meteorsRain.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
            discardedBattery.remove(player);

            MessageSenderService.sendOptional(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

            MessageSenderService.sendOptional("YouAreSafe", sender);

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else {
            MessageSenderService.sendOptional("BatteryNotDiscarded", sender);
            MessageSenderService.sendOptional(new BatteriesToDiscardMessage(1), sender);
        }
    }

    /**
     * Function called to handle current shot
     *
     * @author Gabriele
     */
    private void handleCurrentMeteor(Player player) {

        Game game = gameManager.getGame();

        // Handles current meteor for player
        Component affectedComponent = meteorsRain.checkImpactComponent(game, player, comingMeteor, diceResult);

        // Gets current player sender reference
        Sender sender = gameManager.getSenderByPlayer(player);

        // Destroys affected component
        SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, affectedComponent.getX(), affectedComponent.getY(), sender);
    }
}