package org.progetto.server.controller.events;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.controller.EventController;
import org.progetto.server.controller.LobbyController;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.events.OpenSpace;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class OpenSpaceController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private GameManager gameManager;
    private String phase;
    private int currPlayer;
    private ArrayList<Player> activePlayers;
    private int playerEnginePower;
    private int requestedNumber;

    // =======================
    // CONSTRUCTORS
    // =======================

    public OpenSpaceController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.phase = "START";
        this.currPlayer = 0;
        this.activePlayers = gameManager.getGame().getBoard().getActivePlayers();
        this.playerEnginePower = 0;
        this.requestedNumber = 0;
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
    public void start() throws RemoteException {
        if (phase.equals("START")) {
            phase = "ASK_ENGINES";
            askHowManyEnginesToUse();
        }
    }

    /**
     * Asks current player how many double engines he wants to use
     *
     * @author Gabriele
     */
    private void askHowManyEnginesToUse() throws RemoteException {
        if (phase.equals("ASK_ENGINES")) {

            Player player = activePlayers.get(currPlayer);

            // Gets the sender reference to send a message to player
            SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(player);
            VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(player);

            Sender sender = null;

            if (socketWriter != null) {
                sender = socketWriter;
            } else if (virtualClient != null) {
                sender = virtualClient;
            }

            // Calculates max number of double cannon usable
            int doubleEngineCount = player.getSpaceship().getDoubleEngineCount();
            int batteriesCount = player.getSpaceship().getBatteriesCount();
            int maxUsable;

            if (doubleEngineCount < batteriesCount) {
                maxUsable = doubleEngineCount;
            } else {
                maxUsable = batteriesCount;
            }

            // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
            if (maxUsable == 0) {
                playerEnginePower = player.getSpaceship().getNormalEnginePower();

                if (playerEnginePower > 0) {
                    phase = "EFFECT";
                    eventEffect();

                } else {
                    sender.sendMessage("ZeroEnginePower");
                    LobbyController.broadcastLobbyMessage(new PlayerDefeatedMessage(player.getName()));
                    gameManager.getGame().getBoard().leaveTravel(player);
                }

            } else {

                sender.sendMessage(new HowManyDoubleEnginesMessage(maxUsable));

                phase = "ENGINE_NUMBER";
            }
        }
    }

    /**
     * Receives numbers of double engines to use
     *
     * @author Gabriele
     */
    public void receiveHowManyEnginesToUse(Player player, int num, Sender sender) throws RemoteException {
        if (phase.equals("ENGINE_NUMBER")) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                // Player doesn't want to use double engines
                if (num == 0) {
                    playerEnginePower = player.getSpaceship().getNormalEnginePower();

                    phase = "EFFECT";
                    eventEffect();

                } else if (num <= player.getSpaceship().getDoubleEngineCount() && num <= player.getSpaceship().getBatteriesCount() && num > 0) {
                    requestedNumber = num;
                    playerEnginePower = player.getSpaceship().getNormalEnginePower() + 2 * num;

                    sender.sendMessage(new BatteriesToDiscardMessage(num));

                    phase = "DISCARDED_BATTERIES";

                } else {
                    sender.sendMessage("IncorrectNumber");
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Receives the coordinates of BatteryStorage component from which remove a battery
     *
     * @author Gabriele
     * @param player
     * @param xBatteryStorage
     * @param yBatteryStorage
     * @param sender
     * @throws RemoteException
     */
    public void receiveDiscardedBattery(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException {
        if (phase.equals("DISCARDED_BATTERIES")) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

                if (batteryStorage != null && batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {
                    OpenSpace openSpace = (OpenSpace) gameManager.getGame().getActiveEventCard();

                    // Checks if a battery has been discarded
                    if (openSpace.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                        requestedNumber--;
                        sender.sendMessage("BatteryDiscarded");

                        if (requestedNumber == 0) {
                            phase = "EFFECT";
                            eventEffect();

                        } else {
                            sender.sendMessage(new BatteriesToDiscardMessage(requestedNumber));
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
     * Applies event effect for current player
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void eventEffect() throws RemoteException {
        if (phase.equals("EFFECT")) {
            Player player = activePlayers.get(currPlayer);
            OpenSpace openSpace = (OpenSpace) gameManager.getGame().getActiveEventCard();

            // Event effect applied for single player
            openSpace.moveAhead(gameManager.getGame().getBoard(), player, playerEnginePower);

            // Sends update message
            SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(player);
            VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(player);

            Sender sender = null;

            if (socketWriter != null) {
                sender = socketWriter;
            } else if (virtualClient != null) {
                sender = virtualClient;
            }

            sender.sendMessage(new PlayerMovedAheadMessage(playerEnginePower));
            LobbyController.broadcastLobbyMessageToOthers(new AnotherPlayerMovedAheadMessage(player.getName(), playerEnginePower), sender);

            // Next player
            if (currPlayer < activePlayers.size()) {
                currPlayer++;
                phase = "ASK_ENGINES";
                askHowManyEnginesToUse();
            } else {
                phase = "END";
                end();
            }
        }
    }

    /**
     * Send a message of end card to all players
     *
     * @author Stefano
     * @throws RemoteException
     */
    private void end() throws RemoteException {
        if (phase.equals("END")) {
            LobbyController.broadcastLobbyMessage("This event card is finished");
        }
    }
}