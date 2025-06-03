package org.progetto.server.controller.events;

import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.messages.toClient.Smugglers.AcceptRewardBoxesAndPenaltyDaysMessage;
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
        if (phase.equals(EventPhase.ASK_CANNONS)) {

            for (Player player : activePlayers) {

                gameManager.getGame().setActivePlayer(player);

                Spaceship spaceship = player.getSpaceship();

                // Retrieves sender reference
                Sender sender = gameManager.getSenderByPlayer(player);

                // Checks if card got defeated
                if (defeated) {
                    gameManager.broadcastGameMessage("RaidersDefeated");
                    break;
                }

                // Checks if players is able to win without double cannons
                if (smugglers.battleResult(spaceship.getNormalShootingPower()) == 1) {
                    phase = EventPhase.REWARD_DECISION;
                    MessageSenderService.sendMessage("YouWonBattle", sender);
                    gameManager.broadcastGameMessageToOthers(new AnotherPlayerWonBattleMessage(player.getName()), sender);

                    MessageSenderService.sendMessage(new AcceptRewardBoxesAndPenaltyDaysMessage(smugglers.getRewardBoxes(), smugglers.getPenaltyDays()), sender);

                    gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                    gameManager.getGameThread().resetAndWaitTravelerReady(player);
                    continue;
                }

                // Calculates max number of double cannons usable
                int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

                // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
                if (maxUsable == 0) {
                    playerFirePower = spaceship.getNormalShootingPower();

                    if (smugglers.battleResult(spaceship.getNormalShootingPower()) == -1) {

                        MessageSenderService.sendMessage("YouLostBattle", sender);
                        gameManager.broadcastGameMessageToOthers(new AnotherPlayerLostBattleMessage(player.getName()), sender);

                        // Checks if he has more than a box/battery
                        int maxBoxCount = player.getSpaceship().getBoxCounts()[0] + player.getSpaceship().getBoxCounts()[1] + player.getSpaceship().getBoxCounts()[2] + player.getSpaceship().getBoxCounts()[3];

                        if (maxBoxCount > 0 || player.getSpaceship().getBatteriesCount() > 0) {
                            requestedBoxes = smugglers.getPenaltyBoxes();

                            gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                            phase = EventPhase.PENALTY_EFFECT;
                            penaltyEffect(player, sender);

                            gameManager.getGameThread().resetAndWaitTravelerReady(player);

                        } else {
                            MessageSenderService.sendMessage("NotEnoughBoxesAndBatteries", sender);
                        }
                    } else {
                        MessageSenderService.sendMessage("YouDrewBattle", sender);
                        gameManager.broadcastGameMessageToOthers(new AnotherPlayerDrewBattleMessage(player.getName()), sender);
                    }
                    continue;
                }

                phase = EventPhase.CANNON_NUMBER;
                MessageSenderService.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, smugglers.getFirePowerRequired(), player.getSpaceship().getNormalShootingPower()), sender);

                gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                gameManager.getGameThread().resetAndWaitTravelerReady(player);
            }
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

            phase = EventPhase.BATTLE_RESULT;
            battleResult(player, sender);

        } else if (num <= (spaceship.getFullDoubleCannonCount() + spaceship.getHalfDoubleCannonCount()) && num <= spaceship.getBatteriesCount() && num > 0) {
            requestedBatteries = num;

            // Updates player's firepower based on his decision
            if (num <= spaceship.getFullDoubleCannonCount()) {
                playerFirePower = spaceship.getNormalShootingPower() + 2 * num;
            } else {
                playerFirePower = spaceship.getNormalShootingPower() + 2 * spaceship.getFullDoubleCannonCount() + (num - spaceship.getFullDoubleCannonCount());
            }

            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(num), sender);

            phase = EventPhase.DISCARDED_BATTERIES;

        } else {
            MessageSenderService.sendMessage("IncorrectNumber", sender);
            MessageSenderService.sendMessage(new HowManyDoubleCannonsMessage(spaceship.maxNumberOfDoubleCannonsUsable(), smugglers.getFirePowerRequired(), player.getSpaceship().getNormalShootingPower()), sender);
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

            if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

            } else if (phase.equals(EventPhase.DISCARDED_BATTERIES_FOR_BOXES)) {
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBoxes), sender);
            }
            return;
        }

        Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

        // Checks if component is a battery storage
        if (batteryStorage == null || !batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {
            MessageSenderService.sendMessage("InvalidComponent", sender);

            if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

            } else if (phase.equals(EventPhase.DISCARDED_BATTERIES_FOR_BOXES)) {
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBoxes), sender);
            }
            return;
        }

        if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {

            // Checks if a battery has been discarded
            if (smugglers.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) { // todo: eliminare metodo dal model, vedere gestione alternativa negli eventi con disconnesione fatta
                requestedBatteries--;
                MessageSenderService.sendMessage(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

                if (requestedBatteries == 0) {
                    phase = EventPhase.BATTLE_RESULT;
                    battleResult(player, sender);

                } else {
                    MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);
                }

            } else {
                MessageSenderService.sendMessage("BatteryNotDiscarded", sender);
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);
            }

        } else if (phase.equals(EventPhase.DISCARDED_BATTERIES_FOR_BOXES)) {

            // Checks if a battery has been discarded
            if (smugglers.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                requestedBoxes--;

                MessageSenderService.sendMessage(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

                if (requestedBoxes == 0) {
                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();

                } else if(requestedBoxes > 0){
                    MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBoxes), sender);
                    phase = EventPhase.DISCARDED_BATTERIES_FOR_BOXES;
                }

                if (player.getSpaceship().getBatteriesCount() == 0) {
                    MessageSenderService.sendMessage("NotEnoughBatteries", sender);

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                }

            } else {
                MessageSenderService.sendMessage("BatteryNotDiscarded", sender);
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBoxes), sender);
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
    private void battleResult(Player player, Sender sender) {
        if (phase.equals(EventPhase.BATTLE_RESULT)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                // Calls the battleResult function
                switch (smugglers.battleResult(playerFirePower)){
                    case 1:
                        MessageSenderService.sendMessage("YouWonBattle", sender);
                        gameManager.broadcastGameMessageToOthers(new AnotherPlayerWonBattleMessage(player.getName()), sender);

                        phase = EventPhase.REWARD_DECISION;
                        defeated = true;
                        MessageSenderService.sendMessage(new AcceptRewardBoxesAndPenaltyDaysMessage(smugglers.getRewardBoxes(), smugglers.getPenaltyDays()), sender);
                        break;

                    case -1:
                        MessageSenderService.sendMessage("YouLostBattle", sender);
                        gameManager.broadcastGameMessageToOthers(new AnotherPlayerLostBattleMessage(player.getName()), sender);

                        // Checks if he has more than a box/battery
                        int maxBoxCount = player.getSpaceship().getBoxCounts()[0] + player.getSpaceship().getBoxCounts()[1] + player.getSpaceship().getBoxCounts()[2] + player.getSpaceship().getBoxCounts()[3];

                        if (maxBoxCount > 0 || player.getSpaceship().getBatteriesCount() > 0) {
                            requestedBoxes = smugglers.getPenaltyBoxes();
                            phase = EventPhase.PENALTY_EFFECT;
                            defeated = false;
                            penaltyEffect(player, sender);

                        } else {
                            MessageSenderService.sendMessage("NotEnoughBoxesAndBatteries", sender);

                            player.setIsReady(true, gameManager.getGame());
                            gameManager.getGameThread().notifyThread();
                        }

                        break;

                    case 0:
                        MessageSenderService.sendMessage("YouDrewBattle", sender);
                        gameManager.broadcastGameMessageToOthers(new AnotherPlayerDrewBattleMessage(player.getName()), sender);

                        defeated = false;
                        player.setIsReady(true, gameManager.getGame());
                        gameManager.getGameThread().notifyThread();
                        break;
                }
            }
        }
    }

    /**
     * If the player is defeated he suffers the penalty
     *
     * @author Stefano
     * @param player current player
     * @param sender current sender
     */
    private void penaltyEffect(Player player, Sender sender) {
        if (phase.equals(EventPhase.PENALTY_EFFECT)) {

            // Box currently owned
            int maxBoxCount = player.getSpaceship().getBoxCounts()[0] + player.getSpaceship().getBoxCounts()[1] + player.getSpaceship().getBoxCounts()[2] + player.getSpaceship().getBoxCounts()[3];

            // Checks if he has at least a box to discard
            if (maxBoxCount > 0) {
                MessageSenderService.sendMessage(new BoxToDiscardMessage(requestedBoxes), sender);
                phase = EventPhase.DISCARDED_BOXES;

            } else {
                MessageSenderService.sendMessage("NotEnoughBoxes", sender);
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBoxes), sender);
                phase = EventPhase.DISCARDED_BATTERIES_FOR_BOXES;
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

        Component boxStorage = spaceshipMatrix[yBoxStorage][xBoxStorage];

        // Checks if component is a box storage
        if (boxStorage == null || (!boxStorage.getType().equals(ComponentType.BOX_STORAGE) && !boxStorage.getType().equals(ComponentType.RED_BOX_STORAGE))) {
            MessageSenderService.sendMessage("InvalidComponent", sender);
            MessageSenderService.sendMessage(new BoxToDiscardMessage(requestedBoxes), sender);
            return;
        }

        // Checks if a box has been discarded
        if (smugglers.chooseDiscardedBox(player.getSpaceship(), (BoxStorage) boxStorage, idx)) {
            requestedBoxes--;

            MessageSenderService.sendMessage(new BoxDiscardedMessage(xBoxStorage, yBoxStorage, idx), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerBoxDiscardedMessage(player.getName(), xBoxStorage, yBoxStorage, idx), sender);

            if (requestedBoxes == 0) {
                player.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();

            } else {

                int maxBoxCount = player.getSpaceship().getBoxCounts()[0] + player.getSpaceship().getBoxCounts()[1] + player.getSpaceship().getBoxCounts()[2] + player.getSpaceship().getBoxCounts()[3];

                // Checks if he has at least a box to discard
                if (maxBoxCount > 0) {
                    MessageSenderService.sendMessage(new BoxToDiscardMessage(requestedBoxes), sender);
                    phase = EventPhase.DISCARDED_BOXES;

                // Checks if he has at least a battery to discard
                } else if (player.getSpaceship().getBatteriesCount() > 0) {
                    MessageSenderService.sendMessage("NotEnoughBoxes", sender);
                    MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBoxes), sender);
                    phase = EventPhase.DISCARDED_BATTERIES_FOR_BOXES;

                } else {
                    MessageSenderService.sendMessage("NotEnoughBoxesAndBatteries", sender);

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                }
            }
        } else {
            MessageSenderService.sendMessage("BoxNotDiscarded", sender);
            MessageSenderService.sendMessage(new BoxToDiscardMessage(requestedBoxes), sender);
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
     * @param idxBox chosen
     * @param yBoxStorage coordinate of the component were the box will be placed
     * @param xBoxStorage coordinate of the component were the box will be placed
     * @param idx is where the player want to insert the chosen box
     * @param sender current sender
     */
    @Override
    public void receiveRewardBox(Player player, int idxBox, int xBoxStorage, int yBoxStorage, int idx, Sender sender) {
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
        if (idxBox < -1 || idxBox >= rewardBoxes.size()) {
            MessageSenderService.sendMessage("IncorrectRewardIndex", sender);
            MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
            return;
        }

        // Checks if player wants to leave
        if (idxBox == -1) {
            leaveReward(player);
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

        Box box = rewardBoxes.get(idxBox);

        // Checks that reward box is placed correctly in given storage
        try{
            smugglers.chooseRewardBox(player.getSpaceship(), (BoxStorage) component, box, idx);

            rewardBoxes.remove(box);
            MessageSenderService.sendMessage("BoxChosen", sender);
        } catch (IllegalStateException e) {
            MessageSenderService.sendMessage(e.getMessage(), sender);
        }

        // Checks if all boxes were chosen
        if (rewardBoxes.isEmpty()) {
            MessageSenderService.sendMessage("EmptyReward", sender);
            leaveReward(player);

        } else {
            MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
        }
    }

    /**
     * Function called if the player wants to leave the remaining reward
     *
     * @author Gabriele
     * @param player current player
     * @throws IllegalStateException
     */
    private void leaveReward(Player player) throws IllegalStateException {
        if (phase.equals(EventPhase.CHOOSE_BOX)) {

            // Checks that current player is trying to get reward the reward box
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                // Calls penalty function
                phase = EventPhase.PENALTY_DAYS;
                penaltyDays();
            }
        }
    }

    /**
     * If the player accepted, he receives the reward and loses the penalty days
     *
     * @author Stefano
     */
    private void penaltyDays() {
        if (phase.equals(EventPhase.PENALTY_DAYS)) {

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
    }
}