package org.progetto.server.controller.events;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.EventCommon.IncomingProjectileMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.controller.LobbyController;
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
    private int diceResult;
    private ArrayList<Projectile> meteors;
    private ArrayList<Player> shieldProtectedPlayers;
    private ArrayList<Player> shieldNotProtectedPlayers;
    private ArrayList<Player> discardedBatteryForShield;


    // =======================
    // CONSTRUCTORS
    // =======================

    public MeteorsRainController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.meteorsRain = (MeteorsRain) gameManager.getGame().getActiveEventCard();
        this.phase = "START";
        this.activePlayers = gameManager.getGame().getBoard().getActivePlayers();
        this.meteors = new ArrayList<>(meteorsRain.getMeteors());
        this.diceResult = 0;
        this.shieldProtectedPlayers = new ArrayList<>();
        this.shieldNotProtectedPlayers = new ArrayList<>();
        this.discardedBatteryForShield = new ArrayList<>();
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Starts event card effect
     *
     * @author Lorenzo
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
     * @author Lorenzo
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
     * @throws RemoteException
     * @author Lorenzo
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
     * @param player is the one that needs to trow the dices
     * @param sender
     * @throws RemoteException
     * @author Lorenzo
     */
    @Override
    public void rollDice(Player player, Sender sender) throws RemoteException {
        if (phase.equals("ROLL_DICE")) {

            diceResult = player.rollDice();

            sender.sendMessage(new DiceResultMessage(diceResult));
            LobbyController.broadcastLobbyMessageToOthers(new AnotherPlayerDiceResultMessage(activePlayers.getFirst().getName(), diceResult), sender);

            if (meteors.getFirst().getSize().equals(ProjectileSize.SMALL)) {
                phase = "ASK_SHIELDS";
                askToUseShields();

            } else {
                phase = "HANDLE_SHOT";
                handleShot();
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Asks players if they want to use a shield to protect the ship
     *
     * @author Lorenzo
     */
    private void askToUseShields() throws RemoteException {

        if (phase.equals("ASK_SHIELDS")) {
            for (Player player : activePlayers) {

                // Checks if current player has a shield that covers that direction
                boolean hasShield = meteorsRain.checkShields(player, meteors.getFirst());

                // Asks current player if he wants to use a shield
                SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(player);
                VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(player);

                Sender sender = null;

                if (socketWriter != null) {
                    sender = socketWriter;
                } else if (virtualClient != null) {
                    sender = virtualClient;
                }

                if (hasShield && player.getSpaceship().getBatteriesCount() > 0) {
                    sender.sendMessage("AskToUseShield");

                } else {
                    shieldNotProtectedPlayers.add(player);
                    sender.sendMessage("NoShieldAvailable");
                }
            }

            if (shieldNotProtectedPlayers.size() == activePlayers.size()) {
                phase = "HANDLE_SHOT";
                handleShot();

            } else {
                phase = "SHIELD_DECISION";
            }
        }
    }

    /**
     * TODO: AGGIUNGERE COMMENTO!!!
     *
     * @author Lorenzo
     * @param player is the one that send the decision about using shields
     * @param response is the given decision
     * @param sender
     * @throws RemoteException
     */
    public synchronized void receiveShieldDecision(Player player, String response, Sender sender) throws RemoteException {
        if (phase.equals("SHIELD_DECISION")) {

            // Checks if it is not part of non-protected player, and it is not already contained in protected one list
            if (!shieldNotProtectedPlayers.contains(player) && !shieldProtectedPlayers.contains(player)) {

                String upperCaseResponse = response.toUpperCase();

                switch (upperCaseResponse) {
                    case "YES":
                        shieldProtectedPlayers.add(player);
                        discardedBatteryForShield.add(player);
                        break;

                    case "NO":
                        shieldNotProtectedPlayers.add(player);
                        break;

                    default:
                        sender.sendMessage("IncorrectResponse");
                        break;
                }

                // Checks that every player has given his preference
                if (shieldNotProtectedPlayers.size() + shieldProtectedPlayers.size() == activePlayers.size()) {

                    if (!shieldProtectedPlayers.isEmpty()) {

                        // Asks for a battery to each protected player
                        for (Player protectedPlayer : shieldProtectedPlayers) {
                            SocketWriter socketWriterProtected = gameManager.getSocketWriterByPlayer(protectedPlayer);
                            VirtualClient virtualClientProtected = gameManager.getVirtualClientByPlayer(protectedPlayer);

                            Sender senderProtected = null;

                            if (socketWriterProtected != null) {
                                senderProtected = socketWriterProtected;
                            } else if (virtualClientProtected != null) {
                                senderProtected = virtualClientProtected;
                            }

                            senderProtected.sendMessage(new BatteriesToDiscardMessage(1));
                        }

                        phase = "SHIELD_BATTERY";

                    } else {
                        phase = "HANDLE_SHOT";
                        handleShot();
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
     * Receives the coordinates of BatteryStorage component from which remove a battery to activate shield
     *
     * @author Lorenzo
     * @param player that send the decision about the battery position
     * @param xBatteryStorage coordinate of the storage
     * @param yBatteryStorage coordinate of the storage
     * @param sender
     * @throws RemoteException
     */
    public synchronized void receiveShieldBattery(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException {
        if (phase.equals("SHIELD_BATTERY")) {

            // Checks if the player that calls the methods has to discard a battery to activate a shield
            if (discardedBatteryForShield.contains(player)) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

                if (batteryStorage != null && batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {

                    // Checks if a battery has been discarded
                    if (meteorsRain.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {

                        discardedBatteryForShield.remove(player);
                        sender.sendMessage("BatteryDiscarded");

                        if (discardedBatteryForShield.isEmpty()) {
                            phase = "HANDLE_SHOT";
                            handleShot();
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
     * @author Lorenzo
     * @throws RemoteException
     */
    private void handleShot() throws RemoteException {
        if (phase.equals("HANDLE_SHOT")) {

            Game game = gameManager.getGame();
            Projectile shot = meteors.getFirst();

            // For each non-protected player handles penalty shot
            for (Player shieldNotProtectedPlayer : shieldNotProtectedPlayers) {
                Component hitComponent = meteorsRain.checkImpactComponent(game, shieldNotProtectedPlayer, shot, diceResult);

                // Check if a small meteor hit an exposed connector
                if((shot.getSize().equals(ProjectileSize.SMALL)) && (hitComponent.getConnections()[diceResult] > 0)){

                    // TODO: complete destruction of a component and handle spaceship params update

                }

                // Gets current player sender reference
                SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(shieldNotProtectedPlayer);
                VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(shieldNotProtectedPlayer);

                Sender sender = null;

                if (socketWriter != null) {
                    sender = socketWriter;
                } else if (virtualClient != null) {
                    sender = virtualClient;
                }

                // Sends two types of messages based on the shot's result
                if (hitComponent != null) {
                    sender.sendMessage(new DestroyedComponentMessage(hitComponent.getY(), hitComponent.getX()));
                    LobbyController.broadcastLobbyMessageToOthers(new AnotherPlayerDestroyedComponentMessage(shieldNotProtectedPlayer.getName(), hitComponent.getY(), hitComponent.getX()), sender);

                } else {
                    LobbyController.broadcastLobbyMessage("NothingGotDestroyed");
                }
            }

            // Resets elaboration attributes
            shieldProtectedPlayers.clear();
            shieldNotProtectedPlayers.clear();
            discardedBatteryForShield.clear();

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
     * @author Lorenzo
     * @throws RemoteException
     */
    private void end() throws RemoteException {
        if (phase.equals("END")) {
            LobbyController.broadcastLobbyMessage("This event card is finished");
        }
    }
}
