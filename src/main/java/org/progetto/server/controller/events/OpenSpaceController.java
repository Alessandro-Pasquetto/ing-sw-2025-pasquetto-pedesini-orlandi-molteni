package org.progetto.server.controller.events;

import org.progetto.messages.toClient.EventCommon.BatteriesToDiscardMessage;
import org.progetto.messages.toClient.EventCommon.HowManyDoubleEnginesMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.messages.toClient.OpenSpace.AnotherPlayerMovedAheadMessage;
import org.progetto.messages.toClient.OpenSpace.PlayerMovedAheadMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Board;
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

    OpenSpace openSpace;
    private final ArrayList<Player> activePlayers;
    private int playerEnginePower;
    private int requestedNumber;

    // =======================
    // CONSTRUCTORS
    // =======================

    public OpenSpaceController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.openSpace = (OpenSpace) gameManager.getGame().getActiveEventCard();
        this.phase = EventPhase.START;
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();
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
    public void start() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.START)){
            phase = EventPhase.ASK_ENGINES;
            askHowManyEnginesToUse();
        }
    }

    /**
     * Asks current player how many double engines he wants to use
     *
     * @author Gabriele
     */
    private void askHowManyEnginesToUse() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.ASK_ENGINES)) {

            System.out.println("Asking engines");

            for (Player player : activePlayers) {

                gameManager.getGame().setActivePlayer(player);

                // Gets the sender reference to send a message to player
                Sender sender = gameManager.getSenderByPlayer(player);

                // Calculates max number of double engine usable
                int maxUsable = player.getSpaceship().maxNumberOfDoubleEnginesUsable();

                // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
                if (maxUsable == 0)
                    playerEnginePower = player.getSpaceship().getNormalEnginePower();
                else {
                    System.out.println("Waiting for HowManyDoubleEngines");
                    sender.sendMessage(new HowManyDoubleEnginesMessage(maxUsable));
                    phase = EventPhase.ENGINE_NUMBER;

                    gameManager.getGameThread().resetAndWaitPlayerReady(player);
                }

                phase = EventPhase.EFFECT;
                eventEffect();
            }
        }
    }

    /**
     * Receives numbers of double engines to use
     *
     * @author Gabriele
     */
    @Override
    public void receiveHowManyEnginesToUse(Player player, int num, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.ENGINE_NUMBER)) {

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
                    phase = EventPhase.DISCARDED_BATTERIES;
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
    @Override
    public void receiveDiscardedBatteries(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();
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
        if (phase.equals(EventPhase.EFFECT)) {

            Player player = gameManager.getGame().getActivePlayer();
            Board board = gameManager.getGame().getBoard();

            // Sends update message
            Sender sender = gameManager.getSenderByPlayer(player);

            // Checks if player has an engine power greater than zero
            if (playerEnginePower > 0) {
                // Event effect applied for single player
                openSpace.moveAhead(gameManager.getGame().getBoard(), player, playerEnginePower);

                sender.sendMessage(new PlayerMovedAheadMessage(playerEnginePower));
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedAheadMessage(player.getName(), playerEnginePower), sender);

            } else {
                sender.sendMessage("NoEnginePower");
                sender.sendMessage("youHaveLeftTheFlight");
                gameManager.broadcastGameMessageToOthers(new PlayerDefeatedMessage(player.getName()), sender);
                board.leaveTravel(player);
            }
        }
    }
}