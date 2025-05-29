package org.progetto.server.controller.events;

import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.messages.toClient.OpenSpace.AnotherPlayerMovedAheadMessage;
import org.progetto.messages.toClient.OpenSpace.PlayerMovedAheadMessage;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
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

    private final OpenSpace openSpace;
    private int playerEnginePower;
    private int requestedNumber;
    private final ArrayList<BatteryStorage> batteryStorages;

    // =======================
    // CONSTRUCTORS
    // =======================

    public OpenSpaceController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.openSpace = (OpenSpace) gameManager.getGame().getActiveEventCard();
        this.phase = EventPhase.START;
        this.playerEnginePower = 0;
        this.requestedNumber = 0;
        this.batteryStorages = new ArrayList<>();
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
    public void start(){
        if (!phase.equals(EventPhase.START))
            throw new IllegalStateException("IncorrectPhase");

        phase = EventPhase.ASK_ENGINES;
        askHowManyEnginesToUse();
    }

    /**
     * Asks current player how many double engines he wants to use
     *
     * @author Gabriele
     */
    private void askHowManyEnginesToUse() {
        if (!phase.equals(EventPhase.ASK_ENGINES))
            throw new IllegalStateException("IncorrectPhase");

        ArrayList<Player> activePlayers = gameManager.getGame().getBoard().getCopyTravelers();

        for (Player player : activePlayers) {
            gameManager.getGame().setActivePlayer(player);
            batteryStorages.clear();

            // Gets the sender reference to send a message to player
            Sender sender = gameManager.getSenderByPlayer(player);

            // Calculates max number of double engine usable
            int maxUsable = player.getSpaceship().maxNumberOfDoubleEnginesUsable();

            System.out.println(maxUsable);

            // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
            if (maxUsable == 0)
                playerEnginePower = player.getSpaceship().getNormalEnginePower();
            else{
                gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                try{
                    phase = EventPhase.ENGINE_NUMBER;
                    sender.sendMessage(new HowManyDoubleEnginesMessage(maxUsable, player.getSpaceship().getNormalEnginePower()));

                    gameManager.getGameThread().resetAndWaitTravelerReady(player);

                    // If the player is disconnected
                    if(!player.getIsReady())
                        playerEnginePower = player.getSpaceship().getNormalEnginePower();

                }catch (Exception e){
                    playerEnginePower = player.getSpaceship().getNormalEnginePower();
                }
            }

            phase = EventPhase.EFFECT;
            eventEffect();
        }
    }

    /**
     * Receives numbers of double engines to use
     *
     * @author Gabriele
     * @param player current player
     * @param num number of double engines player want to use
     * @param sender current sender
     * @throws RemoteException
     */
    @Override
    public void receiveHowManyEnginesToUse(Player player, int num, Sender sender) throws RemoteException {

        if (!phase.equals(EventPhase.ENGINE_NUMBER)) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            sender.sendMessage("NotYourTurn");
            return;
        }

        Spaceship spaceship = player.getSpaceship();
        if(num < 0 || num > spaceship.getDoubleEngineCount() || num > spaceship.getBatteriesCount()){
            sender.sendMessage("IncorrectNumber");
            int maxUsable = player.getSpaceship().maxNumberOfDoubleEnginesUsable();
            sender.sendMessage(new HowManyDoubleEnginesMessage(maxUsable, player.getSpaceship().getNormalEnginePower()));
            return;
        }

        // Player doesn't want to use double engines
        if (num == 0) {
            playerEnginePower = player.getSpaceship().getNormalEnginePower();

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else {
            requestedNumber = num;
            playerEnginePower = player.getSpaceship().getNormalEnginePower() + 2 * num;

            System.out.println("Waiting for BatteriesToDiscard");
            phase = EventPhase.DISCARDED_BATTERIES;

            sender.sendMessage(new BatteriesToDiscardMessage(num));
        }
    }

    /**
     * Receives the coordinates of BatteryStorage component from which remove a battery
     *
     * @author Gabriele
     * @param player current player
     * @param xBatteryStorage x coordinate of chosen battery storage
     * @param yBatteryStorage y coordinate of chosen battery storage
     * @param sender current sender
     * @throws RemoteException
     */
    @Override
    public void receiveDiscardedBatteries(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException {
        if (!phase.equals(EventPhase.DISCARDED_BATTERIES)) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            sender.sendMessage("NotYourTurn");
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        if(xBatteryStorage < 0 || yBatteryStorage < 0 || xBatteryStorage >= spaceshipMatrix[0].length || yBatteryStorage >= spaceshipMatrix.length){
            sender.sendMessage("InvalidCoordinates");
            sender.sendMessage(new BatteriesToDiscardMessage(requestedNumber));
            return;
        }

        Component batteryStorageComp = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

        if (batteryStorageComp == null || !batteryStorageComp.getType().equals(ComponentType.BATTERY_STORAGE)) {
            sender.sendMessage("InvalidCoordinates");
            sender.sendMessage(new BatteriesToDiscardMessage(requestedNumber));
            return;
        }

        BatteryStorage batteryStorage = (BatteryStorage) batteryStorageComp;
        // Checks if a battery has been discarded
        if(batteryStorage.getItemsCount() == 0) {
            sender.sendMessage("EmptyBatteryStorage");
            sender.sendMessage(new BatteriesToDiscardMessage(requestedNumber));
            return;
        }

        batteryStorages.add(batteryStorage);
        requestedNumber--;

        sender.sendMessage(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage));
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

        if (requestedNumber == 0) {

            if(!batteryStorages.isEmpty()){
                for (BatteryStorage component : batteryStorages) {
                    component.decrementItemsCount(player.getSpaceship(), 1);
                }

                // Update spaceship to remove highlight components when it's not my turn.
                // For others, it's used to reload the spaceship in case they got disconnected while it was discarding.
                gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));
            }

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
        } else
            sender.sendMessage(new BatteriesToDiscardMessage(requestedNumber));
    }

    /**
     * Applies event effect for current player
     *
     * @author Gabriele
     */
    private void eventEffect() {
        if (!phase.equals(EventPhase.EFFECT))
            throw new IllegalStateException("IncorrectPhase");

        Player player = gameManager.getGame().getActivePlayer();
        Board board = gameManager.getGame().getBoard();

        Sender sender = gameManager.getSenderByPlayer(player);

        // Checks if player has an engine power greater than zero
        if (playerEnginePower > 0) {

            // Event effect applied for single player
            openSpace.moveAhead(gameManager.getGame().getBoard(), player, playerEnginePower);

            try {
                sender.sendMessage(new PlayerMovedAheadMessage(playerEnginePower));
            }catch (Exception e){
                System.err.println("Client unreachable");
            }
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedAheadMessage(player.getName(), playerEnginePower), sender);

        } else {
            try {
                sender.sendMessage("NoEnginePower");
            }catch (Exception e){
                System.err.println("Client unreachable");
            }
            gameManager.broadcastGameMessageToOthers(new PlayerDefeatedMessage(player.getName()), sender);
            board.leaveTravel(player);
        }
    }
}