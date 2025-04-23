package org.progetto.server.controller.events;

import org.progetto.messages.toClient.Building.AnotherPlayerDestroyedComponentMessage;
import org.progetto.messages.toClient.Building.DestroyedComponentMessage;
import org.progetto.messages.toClient.EventCommon.AnotherPlayerDiceResultMessage;
import org.progetto.messages.toClient.EventCommon.BatteriesToDiscardMessage;
import org.progetto.messages.toClient.EventCommon.DiceResultMessage;
import org.progetto.messages.toClient.EventCommon.IncomingProjectileMessage;
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

import java.rmi.RemoteException;
import java.util.ArrayList;

public class MeteorsRainController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private MeteorsRain meteorsRain;
    private ArrayList<Player> activePlayers;
    private ArrayList<Projectile> meteors;
    private int diceResult;
    private ArrayList<Player> decisionPlayers;
    private ArrayList<Player> protectedPlayers;
    private ArrayList<Player> notProtectedPlayers;
    private ArrayList<Player> discardedBattery;

    // =======================
    // CONSTRUCTORS
    // =======================

    public MeteorsRainController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.meteorsRain = (MeteorsRain) gameManager.getGame().getActiveEventCard();
        this.phase = EventPhase.START;
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();
        this.meteors = new ArrayList<>(meteorsRain.getMeteors());
        this.diceResult = 0;
        this.decisionPlayers = new ArrayList<>();
        this.protectedPlayers = new ArrayList<>();
        this.notProtectedPlayers = new ArrayList<>();
        this.discardedBattery = new ArrayList<>();
    }

    // =======================
    // SETTERS
    // =======================

    public void setDiceResult(int diceResult) {
        this.diceResult = diceResult;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Starts event card effect
     *
     * @author Gabriele
     * @throws RemoteException
     * @throws InterruptedException
     */
    @Override
    public void start() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.START)) {
            phase = EventPhase.SEND_METEOR;
            sendMeteor();
        }
    }

    /**
     * Send broadcast the incoming meteor information
     *
     * @author Gabriele
     * @throws RemoteException
     * @throws InterruptedException
     */
    private void sendMeteor() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.SEND_METEOR)) {

            for (Projectile meteor : meteors) {

                // Sends to each player information about incoming meteor
                for (Player player : activePlayers) {
                    Sender sender = gameManager.getSenderByPlayer(player);

                    sender.sendMessage(new IncomingProjectileMessage(meteor.getSize(), meteor.getFrom()));
                }

                phase = EventPhase.ASK_ROLL_DICE;
                askToRollDice();

                gameManager.getGameThread().resetAndWaitTravelersReady();

                // Resets elaboration attributes
                decisionPlayers.clear();
                protectedPlayers.clear();
                notProtectedPlayers.clear();
                discardedBattery.clear();
            }
        }
    }

    /**
     * Asks the leader to trow the dices
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void askToRollDice() throws RemoteException {
        if (phase.equals(EventPhase.ASK_ROLL_DICE)) {

            Sender sender = gameManager.getSenderByPlayer(activePlayers.getFirst());

            if (meteors.getFirst().getFrom() == 0 || meteors.getFirst().getFrom() == 2) {
                sender.sendMessage("ThrowDiceToFindColumn");

            } else if (meteors.getFirst().getFrom() == 1 || meteors.getFirst().getFrom() == 3) {
                sender.sendMessage("ThrowDiceToFindRow");
            }

            phase = EventPhase.ROLL_DICE;
        }
    }

    /**
     * Rolls dice and collects the result
     *
     * @author Gabriele
     * @param player is the one that needs to trow the dices
     * @param sender current sender
     * @throws RemoteException
     */
    @Override
    public void rollDice(Player player, Sender sender) throws RemoteException {
        if (!phase.equals(EventPhase.ROLL_DICE)) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        // Checks if the player that calls method is the leader
        if (!player.equals(activePlayers.getFirst())) {
            sender.sendMessage("NotYourTurn");
            return;
        }

        diceResult = player.rollDice();

        sender.sendMessage(new DiceResultMessage(diceResult));
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiceResultMessage(activePlayers.getFirst().getName(), diceResult), sender);

        if (meteors.getFirst().getSize().equals(ProjectileSize.SMALL)) {
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
     * @throws RemoteException
     */
    private void handleSmallMeteor() throws RemoteException {
        if (phase.equals(EventPhase.HANDLE_SMALL_METEOR)) {

            // For each player checks impact point
            for (Player player : activePlayers) {

                // Retrieves current player sender reference
                Sender sender = gameManager.getSenderByPlayer(player);

                // Finds impact component
                Component affectedComponent = meteorsRain.checkImpactComponent(gameManager.getGame(), player, meteors.getFirst(), diceResult);

                // Checks if there is any affected component
                if (affectedComponent == null) {
                    sender.sendMessage("NoComponentHit");

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                    continue;
                }

                // Checks if component have any exposed connector in shot's direction
                if (affectedComponent.getConnections()[meteors.getFirst().getFrom()] == 0) {
                    sender.sendMessage("NoComponentDamaged");

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                    continue;
                }

                // Checks for shields in that direction and at least a battery
                if (meteorsRain.checkShields(player, meteors.getFirst()) && player.getSpaceship().getBatteriesCount() > 0) {
                    decisionPlayers.add(player);

                // Notifies component destruction
                } else {
                    sender.sendMessage("NoShieldAvailable");

                    SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, affectedComponent.getX(), affectedComponent.getY(), sender);

                    sender.sendMessage(new DestroyedComponentMessage(affectedComponent.getX(), affectedComponent.getY()));
                    gameManager.broadcastGameMessageToOthers(new AnotherPlayerDestroyedComponentMessage(player.getName(), affectedComponent.getX(), affectedComponent.getY()), sender);
                }
            }

            // Checks if there is at least one player that can decide to use a shield
            if (!decisionPlayers.isEmpty()) {
                phase = EventPhase.ASK_TO_PROTECT;
                askToProtect();
            }
        }
    }

    /**
     * Handles current small meteor for each player
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void handleBigMeteor() throws RemoteException {
        if (phase.equals(EventPhase.HANDLE_BIG_METEOR)) {

            // For each player checks impact point
            for (Player player : activePlayers) {

                // Retrieves current player sender reference
                Sender sender = gameManager.getSenderByPlayer(player);

                // Finds impact component
                Component affectedComponent = meteorsRain.checkImpactComponent(gameManager.getGame(), player, meteors.getFirst(), diceResult);

                // Checks if there is any affected component
                if (affectedComponent == null) {
                    sender.sendMessage("NoComponentHit");

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                    continue;
                }

                // Checks if component is a cannon positioned in the same direction as the meteor
                if (affectedComponent.getType().equals(ComponentType.CANNON) && affectedComponent.getRotation() == meteors.getFirst().getFrom()) {
                    sender.sendMessage("MeteorDestroyed");

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                    continue;
                }

                // Checks if component is a double cannon positioned in the same direction as the meteor, and at least a battery
                if (affectedComponent.getType().equals(ComponentType.DOUBLE_CANNON) && affectedComponent.getRotation() == meteors.getFirst().getFrom() && player.getSpaceship().getBatteriesCount() > 0) {
                    decisionPlayers.add(player);

                // Notifies component destruction
                } else {
                    sender.sendMessage("NoCannonAvailable");

                    SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, affectedComponent.getX(), affectedComponent.getY(), sender);

                    sender.sendMessage(new DestroyedComponentMessage(affectedComponent.getX(), affectedComponent.getY()));
                    gameManager.broadcastGameMessageToOthers(new AnotherPlayerDestroyedComponentMessage(player.getName(), affectedComponent.getX(), affectedComponent.getY()), sender);
                }
            }

            // Checks if there is at least one player that can decide to use a double cannon
            if (!decisionPlayers.isEmpty()) {
                phase = EventPhase.ASK_TO_PROTECT;
                askToProtect();
            }
        }
    }

    /**
     * Asks players if they want to protect (shield or double cannon)
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void askToProtect() throws RemoteException {
        if (phase.equals(EventPhase.ASK_TO_PROTECT)) {

            for (Player player : decisionPlayers) {

                // Asks current player if he wants to use a protection
                Sender sender = gameManager.getSenderByPlayer(player);

                if (meteors.getFirst().getSize().equals(ProjectileSize.SMALL))
                    sender.sendMessage("AskToUseShield");
                else
                    sender.sendMessage("AskToUseDoubleCannon");

                phase = EventPhase.PROTECTION_DECISION;
            }
        }
    }

    /**
     * Receives player decision about the usage of shields or double cannon
     *
     * @author Gabriele
     * @param player is the one that send the decision
     * @param response is the given decision
     * @param sender current sender
     * @throws RemoteException
     */
    @Override
    public synchronized void receiveProtectionDecision(Player player, String response, Sender sender) throws RemoteException {
        if (!phase.equals(EventPhase.PROTECTION_DECISION)) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        // Checks if it is not part of non-protected player, and it is not already contained in protected one list
        if (!decisionPlayers.contains(player) || protectedPlayers.contains(player) || notProtectedPlayers.contains(player)) {
            sender.sendMessage("NotYourTurn");
            return;
        }

        String upperCaseResponse = response.toUpperCase();

        switch (upperCaseResponse) {
            case "YES":
                protectedPlayers.add(player);
                discardedBattery.add(player);
                break;

            case "NO":
                notProtectedPlayers.add(player);
                break;

            default:
                sender.sendMessage("IncorrectResponse");
                break;
        }

        // Checks that every player has given his preference
        if (protectedPlayers.size() + notProtectedPlayers.size() == decisionPlayers.size()) {

            if (!protectedPlayers.isEmpty()) {

                // Asks for a battery to each protected player
                for (Player protectedPlayer : protectedPlayers) {
                    Sender senderProtected = gameManager.getSenderByPlayer(protectedPlayer);
                    senderProtected.sendMessage(new BatteriesToDiscardMessage(1));
                }

                phase = EventPhase.PROTECTION_BATTERY;

            } else {
                phase =  EventPhase.HANDLE_CURRENT_METEOR;
                handleCurrentMeteor();
            }
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
     * @throws RemoteException
     */
    @Override
    public synchronized void receiveDiscardedBatteries(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException {
        if (!phase.equals( EventPhase.PROTECTION_BATTERY)) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        // Checks if the player that calls the methods has to discard a battery to activate protection
        if (!discardedBattery.contains(player)) {
            sender.sendMessage("NotYourTurn");
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();

        // Checks if component index is correct
        if (xBatteryStorage < 0 || yBatteryStorage < 0 || yBatteryStorage >= spaceshipMatrix.length || xBatteryStorage >= spaceshipMatrix[0].length ) {
            sender.sendMessage("InvalidCoordinates");
            return;
        }

        Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

        // Checks if component is a battery storage
        if (batteryStorage == null || !batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {
            sender.sendMessage("InvalidComponent");
            return;
        }

        // Checks if a battery has been discarded
        if (meteorsRain.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {

            discardedBattery.remove(player);
            sender.sendMessage("BatteryDiscarded");
            sender.sendMessage("YouAreSafe");

            player.setIsReady(true, gameManager.getGame());

            // Checks that all protected players had discarded a battery
            if (discardedBattery.isEmpty()) {
                phase =  EventPhase.HANDLE_CURRENT_METEOR;
                handleCurrentMeteor();
            }

        } else {
            sender.sendMessage("BatteryNotDiscarded");
        }
    }

    /**
     * Function called to handle current shot
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void handleCurrentMeteor() throws RemoteException {
        if (phase.equals(EventPhase.HANDLE_CURRENT_METEOR)) {

            Game game = gameManager.getGame();
            Projectile meteor = meteors.getFirst();

            // For each non-protected player handles current meteor
            for (Player notProtectedPlayer : notProtectedPlayers) {
                Component affectedComponent = meteorsRain.checkImpactComponent(game, notProtectedPlayer, meteor, diceResult);

                // Gets current player sender reference
                Sender sender = gameManager.getSenderByPlayer(notProtectedPlayer);

                // Destroys affected component
                SpaceshipController.destroyComponentAndCheckValidity(gameManager, notProtectedPlayer, affectedComponent.getX(), affectedComponent.getY(), sender);

                sender.sendMessage(new DestroyedComponentMessage(affectedComponent.getX(), affectedComponent.getY()));
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerDestroyedComponentMessage(notProtectedPlayer.getName(), affectedComponent.getX(), affectedComponent.getY()), sender);
            }
        }
    }
}