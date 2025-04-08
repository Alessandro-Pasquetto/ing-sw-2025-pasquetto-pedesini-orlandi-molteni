package org.progetto.server.controller.events;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.Building.AnotherPlayerDestroyedComponentMessage;
import org.progetto.messages.toClient.Building.DestroyedComponentMessage;
import org.progetto.messages.toClient.EventCommon.IncomingProjectileMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.socket.SocketWriter;
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

    private GameManager gameManager;
    private MeteorsRain meteorsRain;
    private String phase;
    private ArrayList<Player> activePlayers;
    private ArrayList<Projectile> meteors;
    private int diceResult;
    private ArrayList<Player> affectedByMeteor;
    private ArrayList<Player> protectedPlayers;
    private ArrayList<Player> notProtectedPlayers;
    private ArrayList<Player> discardedBattery;

    // =======================
    // CONSTRUCTORS
    // =======================

    public MeteorsRainController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.meteorsRain = (MeteorsRain) gameManager.getGame().getActiveEventCard();
        this.phase = "START";
        this.activePlayers = gameManager.getGame().getBoard().getCopyActivePlayers();
        this.meteors = new ArrayList<>(meteorsRain.getMeteors());
        this.diceResult = 0;
        this.affectedByMeteor = new ArrayList<>();
        this.protectedPlayers = new ArrayList<>();
        this.notProtectedPlayers = new ArrayList<>();
        this.discardedBattery = new ArrayList<>();
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
        phase = "SEND_METEOR";
        sendMeteor();
    }

    /**
     * Send broadcast the incoming meteor information
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void sendMeteor() throws RemoteException {
        if (phase.equals("SEND_METEOR")) {

            if (!meteors.isEmpty()) {

                // Sends to each player information about incoming meteor
                for (Player player : activePlayers) {
                    SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(player);
                    VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(player);

                    Sender sender = null;

                    if (socketWriter != null) {
                        sender = socketWriter;
                    } else if (virtualClient != null) {
                        sender = virtualClient;
                    }

                    sender.sendMessage(new IncomingProjectileMessage(meteors.getFirst().getSize(), meteors.getFirst().getFrom()));
                }

                phase = "ASK_ROLL_DICE";
                askToRollDice();

            } else {

                phase = "END";
                end();

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
        if (phase.equals("ASK_ROLL_DICE")) {

            SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(activePlayers.getFirst());
            VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(activePlayers.getFirst());

            Sender sender = null;

            if (socketWriter != null) {
                sender = socketWriter;
            } else if (virtualClient != null) {
                sender = virtualClient;
            }

            if (meteors.getFirst().getFrom() == 0 || meteors.getFirst().getFrom() == 2) {
                sender.sendMessage("ThrowDiceToFindColumn");

            } else if (meteors.getFirst().getFrom() == 1 || meteors.getFirst().getFrom() == 3) {
                sender.sendMessage("ThrowDiceToFindRow");
            }

            phase = "ROLL_DICE";
        }
    }

    /**
     * Rolls dice and collects the result
     *
     * @author Gabriele
     * @param player is the one that needs to trow the dices
     * @param sender
     * @throws RemoteException
     */
    @Override
    public void rollDice(Player player, Sender sender) throws RemoteException {
        if (phase.equals("ROLL_DICE")) {

            // Checks if the player that calls method is the leader
            if (player.equals(activePlayers.getFirst())) {

                diceResult = player.rollDice();

                sender.sendMessage(new DiceResultMessage(diceResult));
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiceResultMessage(activePlayers.getFirst().getName(), diceResult), sender);

                if (meteors.getFirst().getSize().equals(ProjectileSize.SMALL)) {
                    phase = "HANDLE_SMALL_METEOR";
                    handleSmallMeteor();

                } else {
                    phase = "HANDLE_BIG_METEOR";
                    handleBigMeteor();
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Handles current small meteor for each player
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void handleSmallMeteor() throws RemoteException {
        if (phase.equals("HANDLE_SMALL_METEOR")) {

            // For each player checks impact point
            for (Player player : activePlayers) {

                // Retrieves current player sender reference
                Sender sender = gameManager.getSenderByPlayer(player);

                // Finds impact component
                Component affectedComponent = meteorsRain.checkImpactComponent(gameManager.getGame(), player, meteors.getFirst(), diceResult);

                // Checks if there is any affected component
                if (affectedComponent == null) {
                    sender.sendMessage("NoComponentHit");

                // Checks if component have any exposed connector in shot's direction
                } else if (affectedComponent.getConnections()[meteors.getFirst().getFrom()] == 0) {
                    sender.sendMessage("NoComponentDamaged");

                } else {
                    // Checks for shields in that direction and at least a battery
                    if (meteorsRain.checkShields(player, meteors.getFirst()) && player.getSpaceship().getBatteriesCount() > 0) {
                        affectedByMeteor.add(player);

                    // Notifies component destruction
                    } else {
                        sender.sendMessage("NoShieldAvailable");

                        // TODO: handle waiting in case of needed decision by player on which part of the ship to hold
                        SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, affectedComponent.getY(), affectedComponent.getX(), sender);

                        sender.sendMessage(new DestroyedComponentMessage(affectedComponent.getY(), affectedComponent.getX()));
                        gameManager.broadcastGameMessageToOthers(new AnotherPlayerDestroyedComponentMessage(player.getName(), affectedComponent.getY(), affectedComponent.getX()), sender);
                    }
                }
            }

            // Checks if there is at least one player that can decide to use a shield
            if (!affectedByMeteor.isEmpty()) {
                phase = "ASK_TO_PROTECT";
                askToProtect();

            } else {
                phase = "HANDLE_CURRENT_METEOR";
                handleCurrentMeteor();
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
        if (phase.equals("HANDLE_BIG_METEOR")) {

            // For each player checks impact point
            for (Player player : activePlayers) {

                // Retrieves current player sender reference
                Sender sender = gameManager.getSenderByPlayer(player);

                // Finds impact component
                Component affectedComponent = meteorsRain.checkImpactComponent(gameManager.getGame(), player, meteors.getFirst(), diceResult);

                // Checks if there is any affected component
                if (affectedComponent == null) {
                    sender.sendMessage("NoComponentHit");

                // Checks if component is a cannon positioned in the same direction as the meteor
                } else if (affectedComponent.getType().equals(ComponentType.CANNON) && affectedComponent.getRotation() == meteors.getFirst().getFrom()) {
                    sender.sendMessage("MeteorDestroyed");

                } else {
                    // Checks if component is a double cannon positioned in the same direction as the meteor, and at least a battery
                    if (affectedComponent.getType().equals(ComponentType.DOUBLE_CANNON) && affectedComponent.getRotation() == meteors.getFirst().getFrom() && player.getSpaceship().getBatteriesCount() > 0) {
                        affectedByMeteor.add(player);

                    // Notifies component destruction
                    } else {
                        sender.sendMessage("NoCannonAvailable");

                        // TODO: handle waiting in case of needed decision by player on which part of the ship to hold
                        SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, affectedComponent.getY(), affectedComponent.getX(), sender);

                        sender.sendMessage(new DestroyedComponentMessage(affectedComponent.getY(), affectedComponent.getX()));
                        gameManager.broadcastGameMessageToOthers(new AnotherPlayerDestroyedComponentMessage(player.getName(), affectedComponent.getY(), affectedComponent.getX()), sender);
                    }
                }
            }

            // Checks if there is at least one player that can decide to use a double cannon
            if (!affectedByMeteor.isEmpty()) {
                phase = "ASK_TO_PROTECT";
                askToProtect();

            } else {
                phase = "HANDLE_CURRENT_METEOR";
                handleCurrentMeteor();
            }

        }
    }

    /**
     * Asks players if they want to protect (shield or double cannon)
     *
     * @author Gabriele
     */
    private void askToProtect() throws RemoteException {
        if (phase.equals("ASK_TO_PROTECT")) {

            for (Player player : affectedByMeteor) {

                // Asks current player if he wants to use a protection
                Sender sender = gameManager.getSenderByPlayer(player);

                if (meteors.getFirst().getSize().equals(ProjectileSize.SMALL)) {
                    sender.sendMessage("AskToUseShield");
                } else {
                    sender.sendMessage("AskToUseDoubleCannon");
                }
            }

            phase = "PROTECTION_DECISION";
        }
    }

    /**
     * Receives player decision about the usage of shields or double cannon
     *
     * @author Gabriele
     * @param player is the one that send the decision
     * @param response is the given decision
     * @param sender
     * @throws RemoteException
     */
    public synchronized void receiveProtectionDecision(Player player, String response, Sender sender) throws RemoteException {
        if (phase.equals("PROTECTION_DECISION")) {

            // Checks if it is not part of non-protected player, and it is not already contained in protected one list
            if (affectedByMeteor.contains(player) && !protectedPlayers.contains(player) && !notProtectedPlayers.contains(player)) {

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
                if (protectedPlayers.size() + notProtectedPlayers.size() == affectedByMeteor.size()) {

                    if (!protectedPlayers.isEmpty()) {

                        // Asks for a battery to each protected player
                        for (Player protectedPlayer : protectedPlayers) {
                            Sender senderProtected = gameManager.getSenderByPlayer(protectedPlayer);
                            senderProtected.sendMessage(new BatteriesToDiscardMessage(1));
                        }

                        phase = "PROTECTION_BATTERY";

                    } else {
                        phase = "HANDLE_CURRENT_METEOR";
                        handleCurrentMeteor();
                    }
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Receives the coordinates of BatteryStorage component from which remove a battery to activate protection
     *
     * @author Gabriele
     * @param player that send the decision about the battery position
     * @param xBatteryStorage coordinate of the storage
     * @param yBatteryStorage coordinate of the storage
     * @param sender
     * @throws RemoteException
     */
    public synchronized void receiveProtectionBattery(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException {
        if (phase.equals("PROTECTION_BATTERY")) {

            // Checks if the player that calls the methods has to discard a battery to activate protection
            if (discardedBattery.contains(player)) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

                if (batteryStorage != null && batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {

                    // Checks if a battery has been discarded
                    if (meteorsRain.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {

                        discardedBattery.remove(player);
                        sender.sendMessage("BatteryDiscarded");
                        sender.sendMessage("YouAreSafe");

                        if (discardedBattery.isEmpty()) {
                            phase = "HANDLE_CURRENT_METEOR";
                            handleCurrentMeteor();
                        }

                    } else {
                        sender.sendMessage("NotEnoughBatteries");
                    }

                } else {
                    sender.sendMessage("InvalidCoordinates");
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Function called to handle current shot
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void handleCurrentMeteor() throws RemoteException {
        if (phase.equals("HANDLE_CURRENT_METEOR")) {

            Game game = gameManager.getGame();
            Projectile meteor = meteors.getFirst();

            // For each non-protected player handles current meteor
            for (Player notProtectedPlayer : notProtectedPlayers) {
                Component affectedComponent = meteorsRain.checkImpactComponent(game, notProtectedPlayer, meteor, diceResult);

                // Gets current player sender reference
                Sender sender = gameManager.getSenderByPlayer(notProtectedPlayer);

                // Destroys affected component
                // TODO: handle waiting in case of needed decision by player on which part of the ship to hold
                SpaceshipController.destroyComponentAndCheckValidity(gameManager, notProtectedPlayer, affectedComponent.getY(), affectedComponent.getX(), sender);

                sender.sendMessage(new DestroyedComponentMessage(affectedComponent.getY(), affectedComponent.getX()));
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerDestroyedComponentMessage(notProtectedPlayer.getName(), affectedComponent.getY(), affectedComponent.getX()), sender);
            }

            // Resets elaboration attributes
            affectedByMeteor.clear();
            protectedPlayers.clear();
            notProtectedPlayers.clear();
            discardedBattery.clear();

            // Removes just handled shot
            meteors.removeFirst();

            // Next shot
            phase = "SEND_METEOR";
            sendMeteor();
        }
    }

    /**
     * Send a message of end card to all players
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void end() throws RemoteException {
        if (phase.equals("END")) {
            gameManager.broadcastGameMessage("This event card is finished");
        }
    }
}
