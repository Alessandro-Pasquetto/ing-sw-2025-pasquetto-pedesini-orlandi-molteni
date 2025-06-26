package org.progetto.server.controller.events;

import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.messages.toClient.OpenSpace.AnotherPlayerMovedAheadMessage;
import org.progetto.messages.toClient.OpenSpace.PlayerMovedAheadMessage;
import org.progetto.messages.toClient.Spaceship.UpdateOtherTravelersShipMessage;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.messages.toClient.Track.UpdateTrackMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.controller.GameController;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.events.OpenSpace;

import java.util.ArrayList;

public class OpenSpaceController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private final OpenSpace openSpace;
    private int playerEnginePower;
    private int requestedNumber;
    private final ArrayList<BatteryStorage> batteryStorages;
    private ArrayList<Player> activePlayers;

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
        this.activePlayers = new ArrayList<>();
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

        phase = EventPhase.ASK_ENGINES;
        askHowManyEnginesToUse();
    }

    @Override
    public boolean isParticipant(Player player){
        return activePlayers.contains(player);
    }

    /**
     * Asks current player how many double engines he wants to use
     *
     * @author Gabriele
     */
    private void askHowManyEnginesToUse() throws InterruptedException {
        if (!phase.equals(EventPhase.ASK_ENGINES))
            throw new IllegalStateException("IncorrectPhase");

        activePlayers = gameManager.getGame().getBoard().getCopyTravelers();

        for (Player player : activePlayers) {

            gameManager.getGame().setActivePlayer(player);
            gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

            // Gets the sender reference to send a message to player
            Sender sender = gameManager.getSenderByPlayer(player);

            // Calculates max number of double engine usable
            int maxUsable = player.getSpaceship().maxNumberOfDoubleEnginesUsable();

            // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
            if (maxUsable == 0)
                playerEnginePower = player.getSpaceship().getNormalEnginePower();

            else{
                batteryStorages.clear();

                phase = EventPhase.ENGINE_NUMBER;
                MessageSenderService.sendMessage(new HowManyDoubleEnginesMessage(maxUsable, player.getSpaceship().getNormalEnginePower()), sender);

                gameManager.getGameThread().resetAndWaitTravelerReady(player);

                // Update spaceship to remove highlight components
                // For others, it's used to reload the spaceship in case of disconnections while he was discarding.
                gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));

                // If the player is disconnected
                if(!player.getIsReady())
                    playerEnginePower = player.getSpaceship().getNormalEnginePower();
            }

            phase = EventPhase.EFFECT;
            eventEffect();
        }

        // Reset activePlayer
        gameManager.getGame().setActivePlayer(null);
    }

    /**
     * Receives numbers of double engines to use
     *
     * @author Gabriele
     * @param player current player
     * @param num number of double engines player want to use
     * @param sender current sender
     */
    @Override
    public void receiveHowManyEnginesToUse(Player player, int num, Sender sender) {

        if (!phase.equals(EventPhase.ENGINE_NUMBER)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        Spaceship spaceship = player.getSpaceship();
        if(num < 0 || num > spaceship.getDoubleEngineCount() || num > spaceship.getBatteriesCount()){
            MessageSenderService.sendMessage("IncorrectNumber", sender);
            int maxUsable = player.getSpaceship().maxNumberOfDoubleEnginesUsable();
            MessageSenderService.sendMessage(new HowManyDoubleEnginesMessage(maxUsable, player.getSpaceship().getNormalEnginePower()), sender);
            return;
        }

        // Player doesn't want to use double engines
        if (num == 0) {
            playerEnginePower = player.getSpaceship().getNormalEnginePower();

            player.setIsReady(true);
            gameManager.getGameThread().notifyThread();

        } else {
            requestedNumber = num;
            playerEnginePower = player.getSpaceship().getNormalEnginePower() + 2 * num;

            phase = EventPhase.DISCARDED_BATTERIES;

            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedNumber), sender);
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
     */
    @Override
    public void receiveDiscardedBatteries(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) {
        if (!phase.equals(EventPhase.DISCARDED_BATTERIES)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        if(xBatteryStorage < 0 || yBatteryStorage < 0 || xBatteryStorage >= spaceshipMatrix[0].length || yBatteryStorage >= spaceshipMatrix.length){
            MessageSenderService.sendMessage("InvalidCoordinates", sender);
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedNumber), sender);
            return;
        }

        Component batteryStorageComp = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

        if (batteryStorageComp == null || !batteryStorageComp.getType().equals(ComponentType.BATTERY_STORAGE)) {
            MessageSenderService.sendMessage("InvalidComponent", sender);
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedNumber), sender);
            return;
        }

        BatteryStorage batteryStorage = (BatteryStorage) batteryStorageComp;

        if(batteryStorage.getItemsCount() == 0) {
            MessageSenderService.sendMessage("EmptyBatteryStorage", sender);
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedNumber), sender);
            return;
        }

        batteryStorages.add(batteryStorage);
        requestedNumber--;

        MessageSenderService.sendMessage(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

        if (requestedNumber == 0) {

            for (BatteryStorage component : batteryStorages) {
                component.decrementItemsCount(player.getSpaceship(), 1);
            }

            player.setIsReady(true);
            gameManager.getGameThread().notifyThread();
        } else
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedNumber), sender);
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

            MessageSenderService.sendMessage(new PlayerMovedAheadMessage(playerEnginePower), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedAheadMessage(player.getName(), playerEnginePower), sender);

        } else {
            MessageSenderService.sendMessage("NoEnginePower", sender);
            gameManager.broadcastGameMessageToOthers(new PlayerDefeatedMessage(player.getName()), sender);

            board.leaveTravel(player);
            gameManager.broadcastGameMessage(new UpdateOtherTravelersShipMessage(gameManager.getGame().getBoard().getCopyTravelers()));
            gameManager.broadcastGameMessage(new UpdateTrackMessage(GameController.getAllPlayersInTrackCopy(gameManager), gameManager.getGame().getBoard().getTrack()));
        }
    }

    @Override
    public void reconnectPlayer(Player player, Sender sender) {
        if(!player.equals(gameManager.getGame().getActivePlayer()))
            return;

        if (phase.equals(EventPhase.ENGINE_NUMBER)){
            int maxUsable = player.getSpaceship().maxNumberOfDoubleEnginesUsable();
            MessageSenderService.sendMessage(new HowManyDoubleEnginesMessage(maxUsable, player.getSpaceship().getNormalEnginePower()), sender);
        }
        else if (phase.equals(EventPhase.DISCARDED_BATTERIES)){

            // Remove batteries already discarded
            for(BatteryStorage batteryStorage : batteryStorages){
                MessageSenderService.sendMessage(new BatteryDiscardedMessage(batteryStorage.getX(), batteryStorage.getY()), sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), batteryStorage.getX(), batteryStorage.getY()), sender);
            }

            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedNumber), sender);
        }
    }
}