package org.progetto.server.controller.events;

import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventCommon.AvailableBoxesMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.Smugglers;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class SmugglersController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private Smugglers smugglers;
    private ArrayList<Player> activePlayers;
    private boolean defeated;
    private float playerFirePower;
    private int requestedBatteries;
    private int requestedBoxes;
    private ArrayList<Box> rewardBoxes;

    // =======================
    // CONSTRUCTORS
    // =======================

    public SmugglersController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.smugglers = (Smugglers) gameManager.getGame().getActiveEventCard();
        this.phase = EventPhase.START;
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();;
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
     * @throws RemoteException
     */
    @Override
    public void start() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.START)) {
            phase = EventPhase.ASK_CANNONS;
            askHowManyCannonsToUse();
        }
    }

    /**
     * Asks current player how many double cannons he wants to use
     *
     * @author Stefano
     * @throws RemoteException
     */
    private void askHowManyCannonsToUse() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.ASK_CANNONS)) {

            for (Player player : activePlayers) {

                gameManager.getGame().setActivePlayer(player);

                Spaceship spaceship = player.getSpaceship();

                // Retrieves sender reference
                Sender sender = gameManager.getSenderByPlayer(player);

                // Checks if players is able to win without double cannons
                if (smugglers.battleResult(player, spaceship.getNormalShootingPower()) == 1) {
                    phase = EventPhase.REWARD_DECISION;
                    sender.sendMessage(new AcceptRewardBoxesAndPenaltyDaysMessage(smugglers.getRewardBoxes(), smugglers.getPenaltyDays()));

                } else {

                    // Calculates max number of double cannons usable
                    int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

                    // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
                    if (maxUsable == 0) {
                        playerFirePower = spaceship.getNormalShootingPower();

                        phase = EventPhase.BATTLE_RESULT;
                        battleResult(player, sender);

                    } else {
                        phase = EventPhase.CANNON_NUMBER;
                        sender.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, smugglers.getFirePowerRequired()));
                    }
                }

                gameManager.getGameThread().resetAndWaitPlayerReady(player);

                // Checks if card got defeated
                if (defeated) {
                    break;
                }
            }
        }
    }

    /**
     * Receives numbers of double cannons to use
     *
     * @author Stefano
     * @param player current player
     * @param num
     * @param sender
     * @throws RemoteException
     */
    public void receiveHowManyCannonsToUse(Player player, int num, Sender sender) throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.CANNON_NUMBER)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

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

                    sender.sendMessage(new BatteriesToDiscardMessage(num));

                    phase = EventPhase.DISCARDED_BATTERIES;

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
     * @author Stefano
     * @param player
     * @param xBatteryStorage
     * @param yBatteryStorage
     * @param sender
     * @throws RemoteException
     */
    public void receiveDiscardedBattery(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();
                Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

                if (batteryStorage != null && batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {

                    // Checks if a battery has been discarded
                    if (smugglers.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                        requestedBatteries--;
                        sender.sendMessage("BatteryDiscarded");

                        if (requestedBatteries == 0) {
                            phase = EventPhase.BATTLE_RESULT;
                            battleResult(player, sender);

                        } else {
                            sender.sendMessage(new BatteriesToDiscardMessage(requestedBatteries));
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
     * Check the result of the battle and goes to a new phase based on the result
     *
     * @author Stefano
     * @param player
     * @param sender
     * @throws RemoteException
     */
    private void battleResult(Player player, Sender sender) throws RemoteException, InterruptedException {
        if (phase.equals("BATTLE_RESULT")) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                // Calls the battleResult function
                switch (smugglers.battleResult(player, playerFirePower)){
                    case 1:
                        phase = EventPhase.REWARD_DECISION;
                        sender.sendMessage(new AcceptRewardBoxesAndPenaltyDaysMessage(smugglers.getRewardBoxes(), smugglers.getPenaltyDays()));
                        break;

                    case -1:
                        requestedBoxes = smugglers.getPenaltyBoxes();

                        phase = EventPhase.PENALTY_EFFECT;
                        penaltyEffect(player, sender);
                        break;

                    case 0:
                        player.setIsReady(true, gameManager.getGame());
                        gameManager.getGameThread().notifyThread();
                        break;
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * If the player is defeated he suffers the penalty
     *
     * @author Stefano
     * @param player
     * @param sender
     * @throws RemoteException
     */
    private void penaltyEffect(Player player, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.PENALTY_EFFECT)) {

            // Box currently owned
            int maxBoxCount = player.getSpaceship().getBoxCounts()[0] + player.getSpaceship().getBoxCounts()[1] + player.getSpaceship().getBoxCounts()[2] + player.getSpaceship().getBoxCounts()[3];

            // Checks if he has at least a box to discard
            if (maxBoxCount >= 1) {
                sender.sendMessage(new BoxToDiscardMessage(requestedBoxes));
                phase = EventPhase.DISCARDED_BOXES;

            } else {

                // Checks if he has at least a battery to discard
                if (player.getSpaceship().getBatteriesCount() > 0) {
                    sender.sendMessage("NotEnoughBoxes");
                    sender.sendMessage(new BatteriesToDiscardMessage(requestedBoxes));
                    phase = EventPhase.DISCARDED_BATTERIES_FOR_BOXES;

                } else {
                    sender.sendMessage("NotEnoughBatteries");

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                }

            }
        }
    }

    /**
     * Receives the coordinates of BoxStorage component from which remove a crew member
     *
     * @author Stefano
     * @param player
     * @param xBoxStorage
     * @param yBoxStorage
     * @param sender
     * @throws RemoteException
     */
    public void receiveDiscardedBox(Player player, int xBoxStorage, int yBoxStorage, int idx, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.DISCARDED_BOXES)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();
                Component boxStorage = spaceshipMatrix[yBoxStorage][xBoxStorage];

                if (boxStorage != null && (boxStorage.getType().equals(ComponentType.BOX_STORAGE) || boxStorage.getType().equals(ComponentType.RED_BOX_STORAGE))) {

                    // Checks if a box has been discarded
                    if (smugglers.chooseDiscardedBox(player.getSpaceship(), (BoxStorage) boxStorage, idx)) {
                        requestedBoxes--;
                        sender.sendMessage("BoxDiscarded");

                        if (requestedBoxes == 0) {
                            gameManager.broadcastGameMessage(new PlayerDefeatedMessage(player.getName()));

                            player.setIsReady(true, gameManager.getGame());
                            gameManager.getGameThread().notifyThread();

                        } else {
                            // Ask for new box/battery to discard
                            phase = EventPhase.PENALTY_EFFECT;
                            penaltyEffect(player, sender);
                        }

                    } else {
                        sender.sendMessage("NotEnoughBoxes");
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
     * Receives the coordinates of BatteryStorage component from which remove a battery
     *
     * @author Stefano
     * @param player
     * @param xBatteryStorage
     * @param yBatteryStorage
     * @param sender
     * @throws RemoteException
     */
    public void receiveDiscardedBatteriesForBoxes(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.DISCARDED_BATTERIES_FOR_BOXES)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();
                Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

                if (batteryStorage != null && batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {

                    // Checks if a battery has been discarded
                    if (smugglers.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                        requestedBoxes--;
                        sender.sendMessage("BatteryDiscarded");

                        if (requestedBoxes == 0) {
                            gameManager.broadcastGameMessage(new PlayerDefeatedMessage(player.getName()));

                            player.setIsReady(true, gameManager.getGame());
                            gameManager.getGameThread().notifyThread();

                        } else {
                            // Ask for new battery to discard
                            phase = EventPhase.PENALTY_EFFECT;
                            penaltyEffect(player, sender);
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
     * Receives response for rewardPenalty
     *
     * @author Stefano
     * @param player
     * @param response
     * @param sender
     * @throws RemoteException
     */
    public void receiveRewardDecision(Player player, String response, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.REWARD_DECISION)) {

            if (player.equals(gameManager.getGame().getActivePlayer())) {

                String upperCaseResponse = response.toUpperCase();

                switch (upperCaseResponse) {
                    case "YES":
                        phase = EventPhase.CHOOSE_BOX;
                        sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
                        break;

                    case "NO":
                        player.setIsReady(true, gameManager.getGame());
                        gameManager.getGameThread().notifyThread();
                        break;

                    default:
                        sender.sendMessage("IncorrectResponse");
                        break;
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Receive the box that the player choose, and it's placement in the component
     * Update the player's view with the new list of available boxes
     *
     * @author Gabriele
     * @param player
     * @param sender
     * @throws RemoteException
     */
    public void receiveRewardBox(Player player, int idxBox, int y, int x, int idx, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.CHOOSE_BOX)) {

            if (player.equals(gameManager.getGame().getActivePlayer())) {

                try {
                    Component[][] matrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();
                    BoxStorage storage = (BoxStorage) matrix[y][x];
                    Box box = rewardBoxes.get(idxBox);

                    // Checks box chosen is contained in rewards list
                    if (rewardBoxes.contains(box)) {

                        // Checks that reward box is placed correctly in given storage
                        if (smugglers.chooseRewardBox(player.getSpaceship(), storage, idx, box)) {
                            sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
                            sender.sendMessage("BoxChosen");

                            rewardBoxes.remove(box);

                        } else {
                            sender.sendMessage("BoxNotChosen");
                        }

                    } else {
                        sender.sendMessage("ChosenBoxNotAvailable");
                    }

                } catch (ClassCastException e) {
                    throw new IllegalStateException("ComponentIsNotAStorage");

                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new IllegalStateException("ComponentIsNotInMatrix");
                }

                // All the boxes are chosen
                if (rewardBoxes.isEmpty()) {
                    leaveReward(gameManager.getGame().getActivePlayer(), sender);
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Function called if the player wants to leave the remaining reward
     *
     * @author Gabriele
     * @param player
     * @param sender
     * @throws RemoteException
     * @throws IllegalStateException
     */
    private void leaveReward(Player player, Sender sender) throws RemoteException, IllegalStateException {
        if (phase.equals(EventPhase.CHOOSE_BOX)) {

            // Checks that current player is trying to get reward the reward box
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                // Calls penalty function
                penaltyDays();
            }
        }
    }

    /**
     * If the player accepted, he receives the reward and loses the penalty days
     *
     * @author Stefano
     * @throws RemoteException
     */
    private void penaltyDays() throws RemoteException {
        if (phase.equals(EventPhase.PENALTY_DAYS)) {

            Player player = gameManager.getGame().getActivePlayer();
            Board board = gameManager.getGame().getBoard();

            // Event effect applied for single player
            smugglers.penalty(gameManager.getGame().getBoard(), player);

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            sender.sendMessage(new PlayerMovedBackwardMessage(smugglers.getPenaltyDays()));
            gameManager.broadcastGameMessage(new AnotherPlayerMovedBackwardMessage(player.getName(), smugglers.getPenaltyDays()));

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
        }
    }
}