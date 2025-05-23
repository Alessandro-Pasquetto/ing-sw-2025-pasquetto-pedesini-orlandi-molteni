package org.progetto.server.controller.events;

import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
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

    private final Slavers slavers;
    private final ArrayList<Player> activePlayers;
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
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();
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
     * @throws InterruptedException
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
     * @throws InterruptedException
     */
    private void askHowManyCannonsToUse() throws RemoteException, InterruptedException {
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
                if (slavers.battleResult(player, spaceship.getNormalShootingPower()) == 1) {
                    phase = EventPhase.REWARD_DECISION;
                    sender.sendMessage("YouWonBattle");
                    sender.sendMessage(new AcceptRewardCreditsAndPenaltyDaysMessage(slavers.getRewardCredits(), slavers.getPenaltyDays()));

                    gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                    gameManager.getGameThread().resetAndWaitPlayerReady(player);
                    continue;
                }

                // Calculates max number of double cannons usable
                int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

                // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
                if (maxUsable == 0) {
                    playerFirePower = spaceship.getNormalShootingPower();

                    // Checks if player lose
                    if (slavers.battleResult(player, spaceship.getNormalShootingPower()) == -1) {
                        phase = EventPhase.PENALTY_EFFECT;
                        sender.sendMessage("YouLostBattle");
                        penaltyEffect(player, sender);

                        gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                        gameManager.getGameThread().resetAndWaitPlayerReady(player);

                    } else {
                        sender.sendMessage("YouDrewBattle");
                    }
                    continue;
                }

                sender.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, slavers.getFirePowerRequired(), player.getSpaceship().getNormalShootingPower()));
                phase = EventPhase.CANNON_NUMBER;

                gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                gameManager.getGameThread().resetAndWaitPlayerReady(player);
            }
        }
    }

    /**
     * Receives numbers of double cannons to use
     *
     * @author Stefano
     * @param player current player
     * @param num number of double cannons player want to use
     * @param sender current player
     * @throws RemoteException
     */
    @Override
    public void receiveHowManyCannonsToUse(Player player, int num, Sender sender) throws RemoteException {
        if (!phase.equals(EventPhase.CANNON_NUMBER)) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            sender.sendMessage("NotYourTurn");
            return;
        }

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
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();
            sender.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, slavers.getFirePowerRequired(), player.getSpaceship().getNormalShootingPower()));
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

        // Checks if component index is correct
        if (xBatteryStorage < 0 || yBatteryStorage < 0 || yBatteryStorage >= spaceshipMatrix.length || xBatteryStorage >= spaceshipMatrix[0].length ) {
            sender.sendMessage("InvalidCoordinates");
            sender.sendMessage(new BatteriesToDiscardMessage(requestedBatteries));
            return;
        }

        Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

        // Checks if component is a battery storage
        if (batteryStorage == null || !batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {
            sender.sendMessage("InvalidComponent");
            sender.sendMessage(new BatteriesToDiscardMessage(requestedBatteries));
            return;
        }

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
            sender.sendMessage("BatteryNotDiscarded");
            sender.sendMessage(new BatteriesToDiscardMessage(requestedBatteries));
        }
    }

    /**
     * Check the result of the battle and goes to a new phase based on the result
     *
     * @author Stefano
     * @param player current player
     * @param sender current sender
     * @throws RemoteException
     */
    private void battleResult(Player player, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.BATTLE_RESULT)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                // Calls the battleResult function
                switch (slavers.battleResult(player, playerFirePower)){
                    case 1:
                        sender.sendMessage("YouWonBattle");
                        phase = EventPhase.REWARD_DECISION;
                        sender.sendMessage(new AcceptRewardCreditsAndPenaltyDaysMessage(slavers.getRewardCredits(), slavers.getPenaltyDays()));
                        defeated = true;
                        break;

                    case -1:
                        sender.sendMessage("YouLostBattle");
                        phase = EventPhase.PENALTY_EFFECT;
                        penaltyEffect(player, sender);
                        break;

                    case 0:
                        sender.sendMessage("YouDrewBattle");
                        phase = EventPhase.ASK_CANNONS;

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
     * @throws RemoteException
     */
    private void penaltyEffect(Player player, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.PENALTY_EFFECT)) {

            if (player.equals(gameManager.getGame().getActivePlayer())) {

                requestedCrew = slavers.getPenaltyCrew();
                sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
                phase = EventPhase.DISCARDED_CREW;
            }
        }
    }

    /**
     * Receives the coordinates of HousingUnit component from which remove a crew member
     *
     * @author Stefano
     * @param player current player
     * @param xHousingUnit x coordinate of chosen housing unit
     * @param yHousingUnit y coordinate of chosen housing unit
     * @param sender current sender
     * @throws RemoteException
     */
    @Override
    public void receiveDiscardedCrew(Player player, int xHousingUnit, int yHousingUnit, Sender sender) throws RemoteException {
        if (!phase.equals(EventPhase.DISCARDED_CREW)) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            sender.sendMessage("NotYourTurn");
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xHousingUnit < 0 || yHousingUnit < 0 || yHousingUnit >= spaceshipMatrix.length || xHousingUnit >= spaceshipMatrix[0].length ) {
            sender.sendMessage("InvalidCoordinates");
            sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
            return;
        }

        Component housingUnit = spaceshipMatrix[yHousingUnit][xHousingUnit];

        // Checks if component is a housing unit
        if (housingUnit == null || (!housingUnit.getType().equals(ComponentType.HOUSING_UNIT) && !housingUnit.getType().equals(ComponentType.CENTRAL_UNIT))) {
            sender.sendMessage("InvalidComponent");
            sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
            return;
        }

        // Checks if a crew member has been discarded

        try{
            slavers.chooseDiscardedCrew(player.getSpaceship(), (HousingUnit) housingUnit);
            requestedCrew--;
            sender.sendMessage("CrewMemberDiscarded");

            if (requestedCrew == 0 || player.getSpaceship().getTotalCrewCount() == 0) {

                player.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();

            } else {
                sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
            }
        }catch (IllegalStateException e){
            sender.sendMessage("CrewMemberNotDiscarded");
            sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
        }
    }

    /**
     * Receives response for rewardPenalty
     *
     * @author Stefano
     * @param player current player
     * @param response player's response
     * @param sender current sender
     * @throws RemoteException
     */
    public void receiveRewardDecision(Player player, String response, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.REWARD_DECISION)) {

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
                        sender.sendMessage(new AcceptRewardCreditsAndPenaltyDaysMessage(slavers.getRewardCredits(), slavers.getPenaltyDays()));
                        break;
                }
            }
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

            // Event effect applied for single player
            slavers.rewardPenalty(gameManager.getGame().getBoard(), player);

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            sender.sendMessage(new PlayerMovedBackwardMessage(slavers.getPenaltyDays()));
            sender.sendMessage(new PlayerGetsCreditsMessage(slavers.getRewardCredits()));
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedBackwardMessage(player.getName(), slavers.getPenaltyDays()), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerGetsCreditsMessage(player.getName(), slavers.getRewardCredits()), sender);

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
        }
    }
}