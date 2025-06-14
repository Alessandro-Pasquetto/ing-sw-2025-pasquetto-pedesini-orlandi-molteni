package org.progetto.server.controller.events;

import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.messages.toClient.Smugglers.AcceptRewardBoxesAndPenaltyDaysMessage;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.Smugglers;

import java.util.ArrayList;

public class SmugglersController extends EventControllerAbstract {

    private record BoxSlot(BoxStorage boxStorage, int idx){}

    // =======================
    // ATTRIBUTES
    // =======================

    private final Smugglers smugglers;
    private final ArrayList<Player> activePlayers;
    private boolean defeated;
    private float playerFirePower;
    private int requestedBatteries;
    private int requestedBoxes;
    private final ArrayList<Box> rewardBoxes;

    private final ArrayList<BatteryStorage> batteryStorages;
    private final ArrayList<BoxSlot> boxSlots;
    private final int[] tempBoxCounts;

    // =======================
    // CONSTRUCTORS
    // =======================

    public SmugglersController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.smugglers = (Smugglers) gameManager.getGame().getActiveEventCard();
        this.phase = EventPhase.START;
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();
        this.defeated = false;
        this.playerFirePower = 0;
        this.requestedBatteries = 0;
        this.requestedBoxes = 0;
        this.rewardBoxes = new ArrayList<>(smugglers.getRewardBoxes());
        this.batteryStorages = new ArrayList<>();
        this.boxSlots = new ArrayList<>();
        this.tempBoxCounts = new int[4];
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Starts event card effect
     *
     * @author Stefano
     * @throws InterruptedException
     */
    @Override
    public void start() throws InterruptedException {
        if (phase.equals(EventPhase.START)) {
            phase = EventPhase.ASK_CANNONS;
            askHowManyCannonsToUse();
        }
    }

    /**
     * Asks current player how many double cannons he wants to use
     *
     * @author Stefano
     * @throws InterruptedException
     */
    private void askHowManyCannonsToUse() throws InterruptedException {
        if (!phase.equals(EventPhase.ASK_CANNONS))
            throw new IllegalStateException("IncorrectPhase");

        for (Player player : activePlayers) {

            // Checks if card got defeated
            if (defeated) {
                gameManager.broadcastGameMessage("RaidersDefeated");
                break;
            }

            gameManager.getGame().setActivePlayer(player);
            gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

            Spaceship spaceship = player.getSpaceship();

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            playerFirePower = spaceship.getNormalShootingPower();

            // Calculates max number of double cannons usable
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

            // If he can use any double cannon, and he doesn't win with normalShootingPower
            if(maxUsable != 0 && smugglers.battleResult(playerFirePower) != 1){
                batteryStorages.clear();
                boxSlots.clear();

                phase = EventPhase.CANNON_NUMBER;
                MessageSenderService.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, smugglers.getFirePowerRequired(), player.getSpaceship().getNormalShootingPower()), sender);

                gameManager.getGameThread().resetAndWaitTravelerReady(player);

                // Update spaceship to remove highlight components
                // For others, it's used to reload the spaceship in case of disconnections while he was discarding.
                gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));

                // If the player is disconnected
                if (!player.getIsReady()){
                    playerFirePower = spaceship.getNormalShootingPower();
                }
            }

            phase = EventPhase.BATTLE_RESULT;
            battleResult(player, sender);
        }
    }

    /**
     * Receives numbers of double cannons to use
     *
     * @author Stefano
     * @param player current player
     * @param num number of double cannon player want to use
     * @param sender current sender
     */
    @Override
    public void receiveHowManyCannonsToUse(Player player, int num, Sender sender) {
        if (!phase.equals(EventPhase.CANNON_NUMBER)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        Spaceship spaceship = player.getSpaceship();

        // Player doesn't want to use double cannons
        if (num == 0) {
            playerFirePower = player.getSpaceship().getNormalShootingPower();

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else if (num <= (spaceship.getFullDoubleCannonCount() + spaceship.getHalfDoubleCannonCount()) && num <= spaceship.getBatteriesCount() && num > 0) {
            requestedBatteries = num;

            // Updates player's firepower based on his decision
            if (num <= spaceship.getFullDoubleCannonCount()) {
                playerFirePower = spaceship.getNormalShootingPower() + 2 * num;
            } else {
                playerFirePower = spaceship.getNormalShootingPower() + 2 * spaceship.getFullDoubleCannonCount() + (num - spaceship.getFullDoubleCannonCount());
            }

            phase = EventPhase.DISCARDED_BATTERIES;
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

        } else {
            MessageSenderService.sendMessage("IncorrectNumber", sender);
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();
            MessageSenderService.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, smugglers.getFirePowerRequired(), player.getSpaceship().getNormalShootingPower()), sender);
        }
    }

    /**
     * Receives the coordinates of BatteryStorage component from which remove a battery
     *
     * @author Stefano
     * @param player current player
     * @param xBatteryStorage x coordinate of chosen battery storage
     * @param yBatteryStorage y coordinate of chosen battery storage
     * @param sender current sender
     */
    @Override
    public void receiveDiscardedBatteries(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) {
        if (!phase.equals(EventPhase.DISCARDED_BATTERIES) && !phase.equals(EventPhase.DISCARDED_BATTERIES_FOR_BOXES)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xBatteryStorage < 0 || yBatteryStorage < 0 || yBatteryStorage >= spaceshipMatrix.length || xBatteryStorage >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendMessage("InvalidCoordinates", sender);
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);
            return;
        }

        Component batteryStorageComp = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

        // Checks if component is a battery storage
        if (batteryStorageComp == null || !batteryStorageComp.getType().equals(ComponentType.BATTERY_STORAGE)) {
            MessageSenderService.sendMessage("InvalidComponent", sender);
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);
            return;
        }

        BatteryStorage batteryStorage = (BatteryStorage) batteryStorageComp;

        if(batteryStorage.getItemsCount() == 0) {
            MessageSenderService.sendMessage("EmptyBatteryStorage", sender);
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);
            return;
        }

        batteryStorages.add(batteryStorage);
        requestedBatteries--;

        MessageSenderService.sendMessage(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

        if (requestedBatteries == 0) {

            for (BatteryStorage component : batteryStorages) {
                component.decrementItemsCount(player.getSpaceship(), 1);
            }

            for (BoxSlot boxSlot : boxSlots) {
                boxSlot.boxStorage().removeBox(player.getSpaceship(), boxSlot.idx());
            }

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else {

            if(phase.equals(EventPhase.DISCARDED_BATTERIES))
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

            else if (phase.equals(EventPhase.DISCARDED_BATTERIES_FOR_BOXES)){
                if(batteryStorages.size() != player.getSpaceship().getBatteriesCount()){
                    MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

                } else{
                    MessageSenderService.sendMessage("YouHaveDiscardedAllBatteries", sender);
                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                }
            }
        }
    }

    /**
     * Check the result of the battle and goes to a new phase based on the result
     *
     * @author Stefano
     * @param player current player
     * @param sender current sender
     */
    private void battleResult(Player player, Sender sender) throws InterruptedException {
        if (!phase.equals(EventPhase.BATTLE_RESULT))
            throw new IllegalStateException("IncorrectPhase");

        // Calls the battleResult function
        switch (smugglers.battleResult(playerFirePower)){
            case 1:
                MessageSenderService.sendMessage("YouWonBattle", sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerWonBattleMessage(player.getName()), sender);
                defeated = true;

                phase = EventPhase.REWARD_DECISION;
                MessageSenderService.sendMessage(new AcceptRewardBoxesAndPenaltyDaysMessage(smugglers.getRewardBoxes(), smugglers.getPenaltyDays()), sender);

                gameManager.getGameThread().resetAndWaitTravelerReady(player);

                // Update spaceship to remove highlight components
                // For others, it's used to reload the spaceship in case of disconnections while he was discarding.
                gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));

                // If the player disconnected during the reward decision
                if(!player.getIsReady())
                    penaltyDays();
                break;

            case 0:
                MessageSenderService.sendMessage("YouDrewBattle", sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerDrewBattleMessage(player.getName()), sender);
                break;

            case -1:
                MessageSenderService.sendMessage("YouLostBattle", sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerLostBattleMessage(player.getName()), sender);

                phase = EventPhase.PENALTY_EFFECT;
                penaltyEffect(player, sender);
                break;
        }
    }

    /**
     * If the player is defeated he suffers the penalty
     *
     * @author Stefano
     * @param player current player
     * @param sender current sender
     */
    private void penaltyEffect(Player player, Sender sender) throws InterruptedException {
        if (!phase.equals(EventPhase.PENALTY_EFFECT))
            throw new IllegalStateException("IncorrectPhase");

        Spaceship spaceship = player.getSpaceship();

        // Checks if he has at least a box/battery
        if (spaceship.getBoxesCount() == 0 && spaceship.getBatteriesCount() == 0) {
            MessageSenderService.sendMessage("NotEnoughBoxesAndBatteries", sender);
            return;
        }

        requestedBoxes = smugglers.getPenaltyBoxes();

        tempBoxCounts[0] = spaceship.getBoxCounts()[0];
        tempBoxCounts[1] = spaceship.getBoxCounts()[1];
        tempBoxCounts[2] = spaceship.getBoxCounts()[2];
        tempBoxCounts[3] = spaceship.getBoxCounts()[3];

        batteryStorages.clear();
        boxSlots.clear();

        // Checks if he has at least a box to discard
        if (spaceship.getBoxesCount() > 0) {

            phase = EventPhase.DISCARDED_BOXES;
            MessageSenderService.sendMessage(new BoxToDiscardMessage(requestedBoxes), sender);

        } else {
            MessageSenderService.sendMessage("NotEnoughBoxes", sender);
            requestedBatteries = requestedBoxes;

            phase = EventPhase.DISCARDED_BATTERIES_FOR_BOXES;
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);
        }

        gameManager.getGameThread().resetAndWaitTravelerReady(player);

        // Update spaceship to remove highlight components
        // For others, it's used to reload the spaceship in case of disconnections while he was discarding.
        gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));

        // If the player disconnected
        if(!player.getIsReady()){

            if(spaceship.getBoxesCount() >= requestedBoxes){
                smugglers.randomDiscardBoxes(spaceship, requestedBoxes);
            }else{

                requestedBatteries = requestedBoxes - spaceship.getBoxesCount();

                smugglers.discardAllBoxes(spaceship);

                smugglers.randomDiscardBatteries(spaceship, Math.min(spaceship.getBatteriesCount(), requestedBatteries));
            }
        }
    }

    /**
     * Receives the coordinates of BoxStorage component from which remove a crew member
     *
     * @author Stefano
     * @param player current player
     * @param xBoxStorage x coordinate of chosen box storage
     * @param yBoxStorage y coordinate of chosen box storage
     * @param sender current sender
     */
    @Override
    public void receiveDiscardedBox(Player player, int xBoxStorage, int yBoxStorage, int idx, Sender sender) {
        if (!phase.equals(EventPhase.DISCARDED_BOXES)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xBoxStorage < 0 || yBoxStorage < 0 || yBoxStorage >= spaceshipMatrix.length || xBoxStorage >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendMessage("InvalidCoordinates", sender);
            MessageSenderService.sendMessage(new BoxToDiscardMessage(requestedBoxes), sender);
            return;
        }

        Component boxStorageComp = spaceshipMatrix[yBoxStorage][xBoxStorage];

        // Checks if component is a box storage
        if (boxStorageComp == null || (!boxStorageComp.getType().equals(ComponentType.BOX_STORAGE) && !boxStorageComp.getType().equals(ComponentType.RED_BOX_STORAGE))) {
            MessageSenderService.sendMessage("InvalidComponent", sender);
            MessageSenderService.sendMessage(new BoxToDiscardMessage(requestedBoxes), sender);
            return;
        }

        BoxStorage boxStorage = (BoxStorage) boxStorageComp;
        Box box = boxStorage.getBoxes()[idx];

        if(box == null) {
            MessageSenderService.sendMessage("EmptyBoxSlot", sender);
            MessageSenderService.sendMessage(new BoxToDiscardMessage(requestedBoxes), sender);
            return;
        }

        if(
            (tempBoxCounts[0] != 0 && !box.equals(Box.RED))     ||
            (tempBoxCounts[0] == 0 && tempBoxCounts[1] != 0 && !box.equals(Box.YELLOW))  ||
            (tempBoxCounts[0] == 0 && tempBoxCounts[1] == 0 && tempBoxCounts[2] != 0 && !box.equals(Box.GREEN))   ||
            (tempBoxCounts[0] == 0 && tempBoxCounts[1] == 0 && tempBoxCounts[2] == 0 && tempBoxCounts[3] != 0 && !box.equals(Box.BLUE)))
        {
            MessageSenderService.sendMessage("IsNotMaxValuableBox", sender);
            MessageSenderService.sendMessage(new BoxToDiscardMessage(requestedBoxes), sender);
            return;
        }

        switch (box){
            case RED:
                tempBoxCounts[0]--;
                break;
            case YELLOW:
                tempBoxCounts[1]--;
                break;
            case GREEN:
                tempBoxCounts[2]--;
                break;
            case BLUE:
                tempBoxCounts[3]--;
                break;
        }

        boxSlots.add(new BoxSlot(boxStorage, idx));
        requestedBoxes--;

        MessageSenderService.sendMessage(new BoxDiscardedMessage(xBoxStorage, yBoxStorage, idx), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerBoxDiscardedMessage(player.getName(), xBoxStorage, yBoxStorage, idx), sender);
        
        if (requestedBoxes == 0) {

            for (BoxSlot boxSlot : boxSlots) {
                boxSlot.boxStorage().removeBox(player.getSpaceship(), boxSlot.idx());
            }

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else {

            if (boxSlots.size() != player.getSpaceship().getBoxesCount()){
                MessageSenderService.sendMessage(new BoxToDiscardMessage(requestedBoxes), sender);

            } else {

                if (player.getSpaceship().getBatteriesCount() > 0) {
                    MessageSenderService.sendMessage("NotEnoughBoxes", sender);
                    requestedBatteries = requestedBoxes;
                    phase = EventPhase.DISCARDED_BATTERIES_FOR_BOXES;
                    MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

                } else {
                    MessageSenderService.sendMessage("NotEnoughBatteries", sender);

                    for (BoxSlot boxSlot : boxSlots) {
                        boxSlot.boxStorage().removeBox(player.getSpaceship(), boxSlot.idx());
                    }

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                }
            }
        }
    }

    /**
     * Receives response for rewardPenalty
     *
     * @author Stefano
     * @param player current player
     * @param response player's response
     * @param sender current sender
     */
    public void receiveRewardDecision(Player player, String response, Sender sender) {
        if (!phase.equals(EventPhase.REWARD_DECISION)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        String upperCaseResponse = response.toUpperCase();

        switch (upperCaseResponse) {
            case "YES":
                phase = EventPhase.CHOOSE_BOX;
                MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
                break;

            case "NO":
                player.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();
                break;

            default:
                MessageSenderService.sendMessage("IncorrectResponse", sender);
                MessageSenderService.sendMessage(new AcceptRewardBoxesAndPenaltyDaysMessage(smugglers.getRewardBoxes(), smugglers.getPenaltyDays()), sender);
                break;
        }
    }

    /**
     * Receive the box that the player choose, and it's placement in the component
     * If player wants to leave he selects idxBox = -1
     *
     * @author Gabriele
     * @param player that choose the box
     * @param rewardIdxBox chosen
     * @param yBoxStorage coordinate of the component were the box will be placed
     * @param xBoxStorage coordinate of the component were the box will be placed
     * @param idx is where the player want to insert the chosen box
     * @param sender current sender
     */
    @Override
    public void receiveRewardBox(Player player, int rewardIdxBox, int xBoxStorage, int yBoxStorage, int idx, Sender sender) {
        if (!phase.equals(EventPhase.CHOOSE_BOX)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks that current player is trying to get reward the reward box
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        // Checks if reward box index is correct
        if (rewardIdxBox < -1 || rewardIdxBox >= rewardBoxes.size()) {
            MessageSenderService.sendMessage("IncorrectRewardIndex", sender);
            MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
            return;
        }

        // Checks if player wants to leave
        if (rewardIdxBox == -1) {
            phase = EventPhase.PENALTY_DAYS;
            penaltyDays();
            return;
        }

        Component[][] matrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xBoxStorage < 0 || yBoxStorage < 0 || yBoxStorage >= matrix.length || xBoxStorage >= matrix[0].length ) {
            MessageSenderService.sendMessage("InvalidCoordinates", sender);
            MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
            return;
        }

        Component component = matrix[yBoxStorage][xBoxStorage];

        // Checks if it is a storage component
        if (component == null || (!component.getType().equals(ComponentType.BOX_STORAGE) && !component.getType().equals(ComponentType.RED_BOX_STORAGE))) {
            MessageSenderService.sendMessage("InvalidComponent", sender);
            MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
            return;
        }

        Box box = rewardBoxes.get(rewardIdxBox);

        // Checks that reward box is placed correctly in given storage
        try{
            smugglers.chooseRewardBox(player.getSpaceship(), (BoxStorage) component, box, idx);

            rewardBoxes.remove(box);
            gameManager.broadcastGameMessage(new BoxAddedMessage(player.getName(), xBoxStorage, yBoxStorage, idx, box));

        } catch (IllegalStateException e) {
            MessageSenderService.sendMessage(e.getMessage(), sender);
        }

        // Checks if all boxes were chosen
        if (rewardBoxes.isEmpty()) {
            MessageSenderService.sendMessage("EmptyReward", sender);
            phase = EventPhase.PENALTY_DAYS;
            penaltyDays();

        } else {
            MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
        }
    }

    /**
     * If the player accepted, he receives the reward and loses the penalty days
     *
     * @author Stefano
     */
    private void penaltyDays() {
        if (!phase.equals(EventPhase.PENALTY_DAYS))
            throw new IllegalStateException("IncorrectPhase");

        Player player = gameManager.getGame().getActivePlayer();

        // Event effect applied for single player
        smugglers.penalty(gameManager.getGame().getBoard(), player);

        // Retrieves sender reference
        Sender sender = gameManager.getSenderByPlayer(player);

        MessageSenderService.sendMessage(new PlayerMovedBackwardMessage(smugglers.getPenaltyDays()), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedBackwardMessage(player.getName(), smugglers.getPenaltyDays()), sender);

        player.setIsReady(true, gameManager.getGame());
        gameManager.getGameThread().notifyThread();
    }


    @Override
    public void reconnectPlayer(Player player, Sender sender) {
        if(!player.equals(gameManager.getGame().getActivePlayer()))
            return;

        if (phase.equals(EventPhase.CANNON_NUMBER)){
            int maxUsable = player.getSpaceship().maxNumberOfDoubleCannonsUsable();
            MessageSenderService.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, smugglers.getFirePowerRequired(), player.getSpaceship().getNormalShootingPower()), sender);
        }
        else if (phase.equals(EventPhase.DISCARDED_BATTERIES) || phase.equals(EventPhase.DISCARDED_BATTERIES_FOR_BOXES)){

            // Remove batteries already discarded
            for(BatteryStorage batteryStorage : batteryStorages){
                MessageSenderService.sendMessage(new BatteryDiscardedMessage(batteryStorage.getX(), batteryStorage.getY()), sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), batteryStorage.getX(), batteryStorage.getY()), sender);
            }

            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);
        }
        else if (phase.equals(EventPhase.REWARD_DECISION)){
            MessageSenderService.sendMessage(new AcceptRewardBoxesAndPenaltyDaysMessage(smugglers.getRewardBoxes(), smugglers.getPenaltyDays()), sender);
        }
        else if (phase.equals(EventPhase.DISCARDED_BOXES)){
            MessageSenderService.sendMessage(new BoxToDiscardMessage(requestedBoxes), sender);
        }
    }
}