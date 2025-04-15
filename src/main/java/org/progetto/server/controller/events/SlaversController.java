package org.progetto.server.controller.events;

import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;
import org.progetto.server.model.events.Slavers;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class SlaversController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private Slavers slavers;
    private ArrayList<Player> activePlayers;
    private boolean defeated;
    private float playerFirePower;
    private int requestedBatteries;
    private int requestedCrew;

    // =======================
    // CONSTRUCTORS
    // =======================

    public SlaversController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.slavers = (Slavers) gameManager.getGame().getActiveEventCard();
        this.phase = EventPhase.START;
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();;
        this.defeated = false;
        this.playerFirePower = 0;
        this.requestedBatteries = 0;
        this.requestedCrew = 0;
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
                if (slavers.battleResult(player, spaceship.getNormalShootingPower()) == 1) {
                    phase = EventPhase.REWARD_DECISION;
                    sender.sendMessage(new AcceptRewardCreditsAndPenaltyDaysMessage(slavers.getRewardCredits(), slavers.getPenaltyDays()));

                } else {

                    // Calculates max number of double cannons usable
                    int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

                    // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
                    if (maxUsable == 0) {
                        playerFirePower = spaceship.getNormalShootingPower();

                        phase = EventPhase.BATTLE_RESULT;
                        battleResult(player, sender);

                    } else {
                        sender.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, slavers.getFirePowerRequired()));

                        phase = EventPhase.CANNON_NUMBER;
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

                } else if (num <= (spaceship.getFullDoubleCannonCount() + spaceship.getHalfDoubleCannonCount()) && num <= player.getSpaceship().getBatteriesCount() && num > 0) {
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
                    if (slavers.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
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
        if (phase.equals(EventPhase.BATTLE_RESULT)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                // Calls the battleResult function
                switch (slavers.battleResult(player, playerFirePower)){
                    case 1:
                        phase = EventPhase.REWARD_DECISION;
                        sender.sendMessage(new AcceptRewardCreditsAndPenaltyDaysMessage(slavers.getRewardCredits(), slavers.getPenaltyDays()));
                        defeated = true;
                        break;

                    case -1:
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
    private void penaltyEffect(Player player, Sender sender) throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.PENALTY_EFFECT)) {

            if (player.equals(gameManager.getGame().getActivePlayer())) {

                requestedCrew = slavers.getPenaltyCrew();

                // Calculates max crew number available to discard
                int maxCrewCount = player.getSpaceship().getTotalCrewCount();

                if (maxCrewCount > slavers.getPenaltyCrew()) {
                    sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
                    phase = EventPhase.DISCARDED_CREW;

                } else {
                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Receives the coordinates of HousingUnit component from which remove a crew member
     *
     * @author Stefano
     * @param player
     * @param xHousingUnit
     * @param yHousingUnit
     * @param sender
     * @throws RemoteException
     */
    public void receiveDiscardedCrew(Player player, int xHousingUnit, int yHousingUnit, Sender sender) throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.DISCARDED_CREW)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();
                Component housingUnit = spaceshipMatrix[yHousingUnit][xHousingUnit];

                if (housingUnit != null && housingUnit.getType().equals(ComponentType.HOUSING_UNIT)) {

                    // Checks if a crew member has been discarded
                    if (slavers.chooseDiscardedCrew(player.getSpaceship(), (HousingUnit) housingUnit)) {
                        requestedCrew--;
                        sender.sendMessage("CrewMemberDiscarded");

                        if (requestedCrew == 0) {

                            player.setIsReady(true, gameManager.getGame());
                            gameManager.getGameThread().notifyThread();

                        } else {
                            sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
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
        if (phase.equals("REWARD_DECISION")) {

            if (player.equals(gameManager.getGame().getActivePlayer())) {

                String upperCaseResponse = response.toUpperCase();

                switch (upperCaseResponse) {
                    case "YES":
                        phase = EventPhase.EFFECT;
                        eventEffect();
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
     * If the player accepted, he receives the reward and loses the penalty days
     *
     * @author Stefano
     * @throws RemoteException
     */
    private void eventEffect() throws RemoteException {
        if (phase.equals(EventPhase.EFFECT)) {

            Player player = gameManager.getGame().getActivePlayer();
            Board board = gameManager.getGame().getBoard();

            // Event effect applied for single player
            slavers.rewardPenalty(gameManager.getGame().getBoard(), player);

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            sender.sendMessage(new PlayerMovedBackwardMessage(slavers.getPenaltyDays()));
            sender.sendMessage(new PlayerGetsCreditsMessage(slavers.getRewardCredits()));
            gameManager.broadcastGameMessage(new AnotherPlayerMovedBackwardMessage(player.getName(), slavers.getPenaltyDays()));
            gameManager.broadcastGameMessage(new AnotherPlayerGetsCreditsMessage(player.getName(), slavers.getRewardCredits()));

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
        }
    }
}