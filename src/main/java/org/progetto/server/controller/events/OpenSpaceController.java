package org.progetto.server.controller.events;

import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
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

    private final GameManager gameManager;
    private String phase;
    private final ArrayList<Player> activePlayers;
    private int playerEnginePower;
    private int requestedNumber;

    // =======================
    // CONSTRUCTORS
    // =======================

    public OpenSpaceController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.phase = "START";
        this.activePlayers = gameManager.getGame().getBoard().getCopyActivePlayers();
        this.playerEnginePower = 0;
        this.requestedNumber = 0;
    }

    // =======================
    // GETTERS
    // =======================

    @Override
    public String getPhase() throws RemoteException {
        return phase;
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
    public void start() throws RemoteException, InterruptedException {
        if (phase.equals("START")){
            phase = "ASK_ENGINES";
            askHowManyEnginesToUse();
        }
    }

    /**
     * Asks current player how many double engines he wants to use
     *
     * @author Gabriele
     */
    private void askHowManyEnginesToUse() throws RemoteException, InterruptedException {
        if (phase.equals("ASK_ENGINES")) {
            System.out.println("Asking engines");

            for (Player player : activePlayers) {

                gameManager.getGame().setActivePlayer(player);

                // Gets the sender reference to send a message to player
                Sender sender = gameManager.getSenderByPlayer(player);

                // Calculates max number of double engine usable
                int maxUsable = player.getSpaceship().maxNumberOfDoubleEnginesUsable();

                // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
                if (maxUsable == 0) {
                    playerEnginePower = player.getSpaceship().getNormalEnginePower();

                    if (playerEnginePower > 0) {
                        phase = "EFFECT";
                        eventEffect();

                    } else {
                        sender.sendMessage("ZeroEnginePower");
                        gameManager.broadcastGameMessage(new PlayerDefeatedMessage(player.getName()));
                        gameManager.getGame().getBoard().leaveTravel(player);
                    }

                } else {
                    System.out.println("Waiting for HowManyDoubleEngines");
                    sender.sendMessage(new HowManyDoubleEnginesMessage(maxUsable));
                    phase = "ENGINE_NUMBER";
                    gameManager.getGameThread().waitPlayerReady(player);

                    phase = "EFFECT";
                    eventEffect();
                }
            }
        }
    }

    /**
     * Receives numbers of double engines to use
     *
     * @author Gabriele
     */
    public void receiveHowManyDoubleEnginesToUse(Player player, int num, Sender sender) throws RemoteException {
        if (phase.equals("ENGINE_NUMBER")) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                // Player doesn't want to use double engines
                if (num == 0) {
                    playerEnginePower = player.getSpaceship().getNormalEnginePower();

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();

                } else if (num <= player.getSpaceship().getDoubleEngineCount() && num <= player.getSpaceship().getBatteriesCount() && num > 0) {
                    requestedNumber = num;
                    playerEnginePower = player.getSpaceship().getNormalEnginePower() + 2 * num;

                    System.out.println("Waiting for BatteriesToDiscard");
                    phase = "DISCARDED_BATTERIES";
                    sender.sendMessage(new BatteriesToDiscardMessage(num));


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
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

                if (batteryStorage != null && batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {
                    OpenSpace openSpace = (OpenSpace) gameManager.getGame().getActiveEventCard();

                    // Checks if a battery has been discarded
                    if (openSpace.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                        requestedNumber--;
                        sender.sendMessage("BatteryDiscarded");

                        if (requestedNumber == 0) {
                            player.setIsReady(true, gameManager.getGame());
                            gameManager.getGameThread().notifyThread();

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
            Player player = gameManager.getGame().getActivePlayer();
            OpenSpace openSpace = (OpenSpace) gameManager.getGame().getActiveEventCard();

            // Event effect applied for single player
            openSpace.moveAhead(gameManager.getGame().getBoard(), player, playerEnginePower);

            // Sends update message
            Sender sender = gameManager.getSenderByPlayer(player);

            sender.sendMessage(new PlayerMovedAheadMessage(playerEnginePower));
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedAheadMessage(player.getName(), playerEnginePower), sender);
        }
    }
}