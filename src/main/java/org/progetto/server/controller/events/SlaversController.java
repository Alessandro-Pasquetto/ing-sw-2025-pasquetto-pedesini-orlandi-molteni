package org.progetto.server.controller.events;

import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.server.connection.MessageSenderService;
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

    private final ArrayList<BatteryStorage> batteryStorages;
    private final ArrayList<HousingUnit> housingUnits;


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
        this.batteryStorages = new ArrayList<>();
        this.housingUnits = new ArrayList<>();
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Starts event card effect
     *
     * @author Stefano
     */
    @Override
    public void start() {
        if (phase.equals(EventPhase.START)) {
            phase = EventPhase.ASK_CANNONS;
            askHowManyCannonsToUse();
        }
    }

    /**
     * Asks current player how many double cannons he wants to use
     *
     * @author Stefano
     */
    private void askHowManyCannonsToUse() {
        if (!phase.equals(EventPhase.ASK_CANNONS))
            throw new IllegalStateException("IncorrectPhase");

        for (Player player : activePlayers) {
            batteryStorages.clear();
            housingUnits.clear();

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
                MessageSenderService.sendOptional("YouWonBattle", sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerWonBattleMessage(player.getName()), sender);
                defeated = true;

                try{
                    MessageSenderService.sendCritical(new AcceptRewardCreditsAndPenaltyDaysMessage(slavers.getRewardCredits(), slavers.getPenaltyDays()), sender);

                    gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                    gameManager.getGameThread().resetAndWaitTravelerReady(player);
                    continue;
                } catch (Exception e) {
                    continue;
                }
            }

            // Calculates max number of double cannons usable
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

            // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
            if (maxUsable == 0) {
                playerFirePower = spaceship.getNormalShootingPower();

                // Checks if player lose
                if (slavers.battleResult(player, spaceship.getNormalShootingPower()) == -1) {
                    phase = EventPhase.PENALTY_EFFECT;
                    MessageSenderService.sendOptional("YouLostBattle", sender);
                    gameManager.broadcastGameMessageToOthers(new AnotherPlayerLostBattleMessage(player.getName()), sender);

                    gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                    try{
                        penaltyEffect(sender);

                        gameManager.getGameThread().resetAndWaitTravelerReady(player);

                        // If the player is disconnected
                        if(!player.getIsReady()){
                            handleLostBattleDisconnection(player, spaceship, sender);
                        }

                    }catch (Exception e) {
                        handleLostBattleDisconnection(player, spaceship, sender);
                    }

                } else {
                    MessageSenderService.sendOptional("YouDrewBattle", sender);
                    gameManager.broadcastGameMessageToOthers(new AnotherPlayerDrewBattleMessage(player.getName()), sender);
                }
                continue;
            }

            try{
                gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                MessageSenderService.sendCritical(new HowManyDoubleCannonsMessage(maxUsable, slavers.getFirePowerRequired(), player.getSpaceship().getNormalShootingPower()), sender);
                phase = EventPhase.CANNON_NUMBER;

                gameManager.getGameThread().resetAndWaitTravelerReady(player);

                // If the player is disconnected and slavers are not defeated (disconnection in rewardDecision)
                if (!player.getIsReady() && !defeated){
                    handleDisconnection(player, spaceship, sender);
                }

            } catch (Exception e) {
                handleDisconnection(player, spaceship, sender);
            }
        }
    }

    private void handleLostBattleDisconnection(Player player, Spaceship spaceship, Sender sender){
        MessageSenderService.sendOptional("YouLostBattle", sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerLostBattleMessage(player.getName()), sender);
        slavers.randomDiscardCrew(spaceship, slavers.getPenaltyCrew());
    }

    private void handleDisconnection(Player player, Spaceship spaceship, Sender sender) {
        if(slavers.battleResult(player, spaceship.getNormalShootingPower()) == -1){
            MessageSenderService.sendOptional("YouLostBattle", sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerLostBattleMessage(player.getName()), sender);
            slavers.randomDiscardCrew(spaceship, slavers.getPenaltyCrew());
        }else{
            MessageSenderService.sendOptional("YouDrewBattle", sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerDrewBattleMessage(player.getName()), sender);
        }
    }

    /**
     * Receives numbers of double cannons to use
     *
     * @author Stefano
     * @param player current player
     * @param num number of double cannons player want to use
     * @param sender current player
     */
    @Override
    public void receiveHowManyCannonsToUse(Player player, int num, Sender sender) {
        if (!phase.equals(EventPhase.CANNON_NUMBER)) {
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
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

            MessageSenderService.sendOptional(new BatteriesToDiscardMessage(num), sender);

            phase = EventPhase.DISCARDED_BATTERIES;

        } else {
            MessageSenderService.sendOptional("IncorrectNumber", sender);
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();
            MessageSenderService.sendOptional(new HowManyDoubleCannonsMessage(maxUsable, slavers.getFirePowerRequired(), player.getSpaceship().getNormalShootingPower()), sender);
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
        if (!phase.equals(EventPhase.DISCARDED_BATTERIES)) {
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xBatteryStorage < 0 || yBatteryStorage < 0 || yBatteryStorage >= spaceshipMatrix.length || xBatteryStorage >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendOptional("InvalidCoordinates", sender);
            MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBatteries), sender);
            return;
        }

        Component batteryStorageComp = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

        // Checks if component is a battery storage
        if (batteryStorageComp == null || !batteryStorageComp.getType().equals(ComponentType.BATTERY_STORAGE)) {
            MessageSenderService.sendOptional("InvalidComponent", sender);
            MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBatteries), sender);
            return;
        }

        BatteryStorage batteryStorage = (BatteryStorage) batteryStorageComp;

        if(batteryStorage.getItemsCount() == 0) {
            MessageSenderService.sendOptional("EmptyBatteryStorage", sender);
            MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBatteries), sender);
            return;
        }

        batteryStorages.add(batteryStorage);
        requestedBatteries--;

        MessageSenderService.sendOptional(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

        if (requestedBatteries == 0) {

            if(!batteryStorages.isEmpty()){
                for (BatteryStorage component : batteryStorages) {
                    component.decrementItemsCount(player.getSpaceship(), 1);
                }

                // Update spaceship to remove highlight components when it's not my turn.
                // For others, it's used to reload the spaceship in case they got disconnected while it was discarding.
                gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));
            }

            phase = EventPhase.BATTLE_RESULT;
            battleResult(player, sender);

        } else {
            MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBatteries), sender);
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
        if (!phase.equals(EventPhase.BATTLE_RESULT))
            throw new IllegalStateException("IncorrectPhase");

        // Checks if the player that calls the methods is also the current one in the controller
        if (player.equals(gameManager.getGame().getActivePlayer())) {

            // Calls the battleResult function
            switch (slavers.battleResult(player, playerFirePower)){
                case 1:
                    MessageSenderService.sendOptional("YouWonBattle", sender);
                    gameManager.broadcastGameMessageToOthers(new AnotherPlayerWonBattleMessage(player.getName()), sender);
                    defeated = true;

                    phase = EventPhase.REWARD_DECISION;
                    MessageSenderService.sendOptional(new AcceptRewardCreditsAndPenaltyDaysMessage(slavers.getRewardCredits(), slavers.getPenaltyDays()), sender);
                    break;

                case -1:
                    MessageSenderService.sendOptional("YouLostBattle", sender);
                    gameManager.broadcastGameMessageToOthers(new AnotherPlayerLostBattleMessage(player.getName()), sender);

                    phase = EventPhase.PENALTY_EFFECT;
                    penaltyEffect(sender);
                    break;

                case 0:
                    MessageSenderService.sendOptional("YouDrewBattle", sender);
                    gameManager.broadcastGameMessageToOthers(new AnotherPlayerDrewBattleMessage(player.getName()), sender);

                    phase = EventPhase.ASK_CANNONS;

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                    break;
            }
        }
    }

    /**
     * If the player is defeated he suffers the penalty
     *
     * @author Stefano
     * @param sender current sender
     */
    private void penaltyEffect(Sender sender){
        if (!phase.equals(EventPhase.PENALTY_EFFECT))
            throw new IllegalStateException("IncorrectPhase");

        requestedCrew = slavers.getPenaltyCrew();

        MessageSenderService.sendOptional(new CrewToDiscardMessage(requestedCrew), sender);
        phase = EventPhase.DISCARDED_CREW;
    }

    /**
     * Receives the coordinates of HousingUnit component from which remove a crew member
     *
     * @author Stefano
     * @param player current player
     * @param xHousingUnit x coordinate of chosen housing unit
     * @param yHousingUnit y coordinate of chosen housing unit
     * @param sender current sender
     */
    @Override
    public void receiveDiscardedCrew(Player player, int xHousingUnit, int yHousingUnit, Sender sender) {
        if (!phase.equals(EventPhase.DISCARDED_CREW)) {
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xHousingUnit < 0 || yHousingUnit < 0 || yHousingUnit >= spaceshipMatrix.length || xHousingUnit >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendOptional("InvalidCoordinates", sender);
            MessageSenderService.sendOptional(new CrewToDiscardMessage(requestedCrew), sender);
            return;
        }

        Component housingUnitComp = spaceshipMatrix[yHousingUnit][xHousingUnit];

        // Checks if component is a housing unit
        if (housingUnitComp == null || (!housingUnitComp.getType().equals(ComponentType.HOUSING_UNIT) && !housingUnitComp.getType().equals(ComponentType.CENTRAL_UNIT))) {
            MessageSenderService.sendOptional("InvalidComponent", sender);
            MessageSenderService.sendOptional(new CrewToDiscardMessage(requestedCrew), sender);
            return;
        }

        HousingUnit housingUnit = (HousingUnit) housingUnitComp;

        if(housingUnit.getCrewCount() == 0) {
            MessageSenderService.sendOptional("EmptyHousingUnit", sender);
            MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedCrew), sender);
            return;
        }

        housingUnits.add(housingUnit);
        requestedCrew--;

        MessageSenderService.sendOptional(new CrewDiscardedMessage(xHousingUnit, yHousingUnit), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerCrewDiscardedMessage(player.getName(), xHousingUnit, yHousingUnit), sender);

        if (requestedCrew == 0 || player.getSpaceship().getTotalCrewCount() == 0) {

            if(!housingUnits.isEmpty()){
                for (HousingUnit component : housingUnits) {
                    slavers.chooseDiscardedCrew(player.getSpaceship(), component);
                }

                // Update spaceship to remove highlight components when it's not my turn.
                // For others, it's used to reload the spaceship in case they got disconnected while it was discarding.
                gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));
            }

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else {
            MessageSenderService.sendOptional(new CrewToDiscardMessage(requestedCrew), sender);
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
                        MessageSenderService.sendOptional("IncorrectResponse", sender);
                        MessageSenderService.sendOptional(new AcceptRewardCreditsAndPenaltyDaysMessage(slavers.getRewardCredits(), slavers.getPenaltyDays()), sender);
                        break;
                }
            }
        }
    }

    /**
     * If the player accepted, he receives the reward and loses the penalty days
     *
     * @author Stefano
     */
    private void eventEffect() {
        if (phase.equals(EventPhase.EFFECT)) {

            Player player = gameManager.getGame().getActivePlayer();

            // Event effect applied for single player
            slavers.rewardPenalty(gameManager.getGame().getBoard(), player);

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            MessageSenderService.sendOptional(new PlayerMovedBackwardMessage(slavers.getPenaltyDays()), sender);
            MessageSenderService.sendOptional(new PlayerGetsCreditsMessage(slavers.getRewardCredits()), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedBackwardMessage(player.getName(), slavers.getPenaltyDays()), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerGetsCreditsMessage(player.getName(), slavers.getRewardCredits()), sender);

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
        }
    }
}