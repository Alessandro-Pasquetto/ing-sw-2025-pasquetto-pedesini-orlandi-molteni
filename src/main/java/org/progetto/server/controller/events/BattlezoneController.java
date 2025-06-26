package org.progetto.server.controller.events;

import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.AffectedComponentMessage;
import org.progetto.messages.toClient.Battlezone.AnotherPlayerGotPenalizedMessage;
import org.progetto.messages.toClient.Battlezone.EvaluatingConditionMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.messages.toClient.Spaceship.UpdateOtherTravelersShipMessage;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.messages.toClient.Track.UpdateTrackMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.controller.GameController;
import org.progetto.server.controller.SpaceshipController;
import org.progetto.server.model.Board;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BattlezoneController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Battlezone battlezone;
    private final ArrayList<Player> activePlayers;
    private final ArrayList<ConditionPenalty> couples;
    private ConditionPenalty currentPenalty;
    private ArrayList<Projectile> penaltyShots;
    private Projectile currentShot;
    private final Map<Player, Integer> tempEnginePower;
    private final Map<Player, Float> tempFirePower;
    private int requestedBatteries;
    private int requestedCrew;
    private int requestedBoxes;
    private int diceResult;

    private final ArrayList<BatteryStorage> batteryStorages;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BattlezoneController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.battlezone = (Battlezone) gameManager.getGame().getActiveEventCard();
        this.phase = EventPhase.START;
        this.couples = new ArrayList<>(this.battlezone.getCouples());
        this.currentPenalty = null;
        this.penaltyShots = new ArrayList<>();
        this.currentShot = null;
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();
        this.tempEnginePower = new HashMap<>();
        this.tempFirePower = new HashMap<>();
        this.requestedBatteries = 0;
        this.requestedCrew = 0;
        this.requestedBoxes = 0;
        this.diceResult = 0;
        this.batteryStorages = new ArrayList<>();
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
        if (!phase.equals(EventPhase.START))
            throw new IllegalStateException("IncorrectPhase");

        phase = EventPhase.CONDITION;
        condition();
    }

    @Override
    public boolean isParticipant(Player player){
        return activePlayers.contains(player);
    }

    /**
     * Handles current condition
     *
     * @author Stefano
     * @throws InterruptedException
     */
    private void condition() throws InterruptedException {
        if (!phase.equals(EventPhase.CONDITION))
            throw new IllegalStateException("IncorrectPhase");

        // Checks if all the couples were evaluated
        for (ConditionPenalty conditionPenalty : couples) {

            currentPenalty = conditionPenalty;

            switch (conditionPenalty.getCondition()){

                case ConditionType.CREWREQUIREMENT:
                    gameManager.broadcastGameMessage(new EvaluatingConditionMessage("Crew"));

                    phase = EventPhase.CREW_COUNT;
                    chooseFewerCrew();
                    break;

                case ConditionType.ENGINEPOWERREQUIREMENT:
                    gameManager.broadcastGameMessage(new EvaluatingConditionMessage("Engine"));

                    phase = EventPhase.ASK_ENGINES;
                    askHowManyEnginesToUse();
                    break;

                case ConditionType.FIREPOWERREQUIREMENT:
                    gameManager.broadcastGameMessage(new EvaluatingConditionMessage("Cannon"));

                    phase = EventPhase.ASK_CANNONS;
                    askHowManyCannonsToUse();
                    break;
            }

            // Reset activePlayer
            gameManager.getGame().setActivePlayer(null);
        }
    }

    /**
     * Finds player with fewer crew
     *
     * @author Gabriele
     * @throws InterruptedException
     */
    private void chooseFewerCrew() throws InterruptedException {
        if (!phase.equals(EventPhase.CREW_COUNT))
            throw new IllegalStateException("IncorrectPhase");

        // Finds player with fewer crew
        Player penaltyPlayer = battlezone.lessPopulatedSpaceship(activePlayers);
        gameManager.getGame().setActivePlayer(penaltyPlayer);

        phase = EventPhase.PENALTY;
        penalty(penaltyPlayer);
    }

    /**
     * Asks current player how many double engines he wants to use
     *
     * @author Stefano
     * @throws InterruptedException
     */
    private void askHowManyEnginesToUse() throws InterruptedException {
        if (!phase.equals(EventPhase.ASK_ENGINES))
            throw new IllegalStateException("IncorrectPhase");

        for (Player player : activePlayers) {

            gameManager.getGame().setActivePlayer(player);
            gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            // Calculates max number of double engine usable
            int maxUsable = player.getSpaceship().maxNumberOfDoubleEnginesUsable();

            // If he can't use any double engine, apply event effect; otherwise, ask how many he wants to use
            if (maxUsable == 0) {
                tempEnginePower.put(player, player.getSpaceship().getNormalEnginePower());

            } else {
                batteryStorages.clear();

                phase = EventPhase.ENGINE_NUMBER;
                MessageSenderService.sendMessage(new HowManyDoubleEnginesMessage(maxUsable, player.getSpaceship().getNormalEnginePower()), sender);

                gameManager.getGameThread().resetAndWaitTravelerReady(player);

                // Update spaceship to remove highlight components
                // For others, it's used to reload the spaceship in case of disconnections while he was discarding.
                gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));

                // If the player is disconnected
                if(!player.getIsReady()) {
                    tempEnginePower.put(player, player.getSpaceship().getNormalEnginePower());
                }
            }
        }

        phase = EventPhase.CHOOSE_WEAKEST_ENGINES;
        chooseWeakestEngines();
    }

    /**
     * Receives numbers of double engines to use
     *
     * @author Stefano
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

        // Player doesn't want to use double engines
        if (num == 0) {
            tempEnginePower.put(player, player.getSpaceship().getNormalEnginePower());

            player.setIsReady(true);
            gameManager.getGameThread().notifyThread();

        } else if (num <= player.getSpaceship().getDoubleEngineCount() && num <= player.getSpaceship().getBatteriesCount() && num > 0) {
            requestedBatteries = num;

            tempEnginePower.put(player, player.getSpaceship().getNormalEnginePower() + 2 * num);

            phase = EventPhase.DISCARDED_BATTERIES;
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(num), sender);

        } else {
            MessageSenderService.sendMessage("IncorrectNumber", sender);
            int maxUsable = player.getSpaceship().maxNumberOfDoubleEnginesUsable();
            MessageSenderService.sendMessage(new HowManyDoubleEnginesMessage(maxUsable, player.getSpaceship().getNormalEnginePower()), sender);
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

            gameManager.getGame().setActivePlayer(player);
            gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

            Spaceship spaceship = player.getSpaceship();

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            // Calculates max number of double cannons usable
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

            // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
            if (maxUsable == 0) {
                tempFirePower.put(player, player.getSpaceship().getNormalShootingPower());

            } else {
                batteryStorages.clear();

                phase = EventPhase.CANNON_NUMBER;
                MessageSenderService.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, 0, player.getSpaceship().getNormalShootingPower()), sender);

                gameManager.getGameThread().resetAndWaitTravelerReady(player);

                // Update spaceship to remove highlight components
                // For others, it's used to reload the spaceship in case of disconnections while he was discarding.
                gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));

                // If the player is disconnected
                if(!player.getIsReady()) {
                    tempFirePower.put(player, player.getSpaceship().getNormalShootingPower());
                }
            }
        }

        phase = EventPhase.CHOOSE_WEAKEST_CANNONS;
        chooseWeakestCannons();
    }

    /**
     * Receives numbers of double cannons to use
     *
     * @author Stefano
     * @param player current player
     * @param num number of cannon player want to use
     * @param sender current sende
     */
    @Override
    public void receiveHowManyCannonsToUse(Player player, int num, Sender sender){
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
            tempFirePower.put(player, player.getSpaceship().getNormalShootingPower());
            player.setIsReady(true);
            gameManager.getGameThread().notifyThread();

        } else if (num <= (spaceship.getFullDoubleCannonCount() + spaceship.getHalfDoubleCannonCount()) && num <= player.getSpaceship().getBatteriesCount() && num > 0) {
            requestedBatteries = num;

            // Updates player's firepower based on his decision
            if (num <= spaceship.getFullDoubleCannonCount()) {
                tempFirePower.put(player, spaceship.getNormalShootingPower() + 2 * num);
            } else {
                tempFirePower.put(player, spaceship.getNormalShootingPower() + 2 * spaceship.getFullDoubleCannonCount() + (num - spaceship.getFullDoubleCannonCount()));
            }

            phase = EventPhase.DISCARDED_BATTERIES;
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(num), sender);

        } else {
            MessageSenderService.sendMessage("IncorrectNumber", sender);
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();
            MessageSenderService.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, 0, player.getSpaceship().getNormalShootingPower()), sender);
        }
    }

    /**
     * Receives the coordinates of BatteryStorage component from which remove a battery
     *
     * @author Stefano
     * @param player current player
     * @param xBatteryStorage x coordinate of chosen battery storage
     * @param yBatteryStorage y coordinate of chosen battery storage
     * @param sender current sende
     */
    @Override
    public void receiveDiscardedBatteries(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender){
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xBatteryStorage < 0 || yBatteryStorage < 0 || yBatteryStorage >= spaceshipMatrix.length || xBatteryStorage >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendMessage("InvalidCoordinates", sender);

            if(phase.equals(EventPhase.SHIELD_BATTERY))
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(1), sender);
            else
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

            return;
        }

        Component batteryStorageComp = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

        // Checks if component is a battery storage
        if (batteryStorageComp == null || !batteryStorageComp.getType().equals(ComponentType.BATTERY_STORAGE)) {
            MessageSenderService.sendMessage("InvalidComponent", sender);

            if(phase.equals(EventPhase.SHIELD_BATTERY))
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(1), sender);
            else
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

            return;
        }

        BatteryStorage batteryStorage = (BatteryStorage) batteryStorageComp;

        if(batteryStorage.getItemsCount() == 0) {
            MessageSenderService.sendMessage("EmptyBatteryStorage", sender);

            if(phase.equals(EventPhase.SHIELD_BATTERY))
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(1), sender);
            else
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

            return;
        }

        if (phase.equals(EventPhase.DISCARDED_BATTERIES) || phase.equals(EventPhase.DISCARDED_BATTERIES_FOR_BOXES)) {

            if(phase.equals(EventPhase.DISCARDED_BATTERIES))
                batteryStorages.add(batteryStorage);
            else if (phase.equals(EventPhase.DISCARDED_BATTERIES_FOR_BOXES))
                batteryStorage.decrementItemsCount(player.getSpaceship(), 1);

            requestedBatteries--;

            MessageSenderService.sendMessage(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

            if (requestedBatteries == 0) {

                for (BatteryStorage component : batteryStorages) {
                    component.decrementItemsCount(player.getSpaceship(), 1);
                }

                player.setIsReady(true);
                gameManager.getGameThread().notifyThread();

            } else {

                if(phase.equals(EventPhase.DISCARDED_BATTERIES))
                    MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

                else if (phase.equals(EventPhase.DISCARDED_BATTERIES_FOR_BOXES)){
                    if(batteryStorages.size() != player.getSpaceship().getBatteriesCount()){
                        MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

                    } else{
                        MessageSenderService.sendMessage("YouHaveDiscardedAllBatteries", sender);
                        player.setIsReady(true);
                        gameManager.getGameThread().notifyThread();
                    }
                }
            }

        } else if(phase.equals(EventPhase.SHIELD_BATTERY)) {

            batteryStorage.decrementItemsCount(player.getSpaceship(), 1);

            MessageSenderService.sendMessage(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

            // Update spaceship to remove highlight components.
            MessageSenderService.sendMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player), sender);

            MessageSenderService.sendMessage("YouAreSafe", sender);

            player.setIsReady(true);
            gameManager.getGameThread().notifyThread();
        }
    }

    /**
     * Finds player with less engine power
     *
     * @author Gabriele
     * @throws InterruptedException
     */
    private void chooseWeakestEngines() throws InterruptedException {
        if (!phase.equals(EventPhase.CHOOSE_WEAKEST_ENGINES))
            throw new IllegalStateException("IncorrectPhase");

        int minEnginePower = Integer.MAX_VALUE;

        Player penaltyPlayer = activePlayers.getFirst();

        for (Player player : activePlayers) {
            // Calculates the current player engine power
            int currEnginePower = tempEnginePower.get(player);

            if (currEnginePower < minEnginePower) {
                minEnginePower = currEnginePower;
                penaltyPlayer = player;
            }
            else if (currEnginePower == minEnginePower) {
                // In case of tie, picks farthest player on the route
                if (player.getPosition() > penaltyPlayer.getPosition()) {
                    penaltyPlayer = player;
                }
            }
        }

        gameManager.getGame().setActivePlayer(penaltyPlayer);

        phase = EventPhase.PENALTY;
        penalty(penaltyPlayer);
    }

    /**
     * Finds player with less firepower
     *
     * @author Gabriele
     * @throws InterruptedException
     */
    private void chooseWeakestCannons() throws InterruptedException {
        if (!phase.equals(EventPhase.CHOOSE_WEAKEST_CANNONS))
            throw new IllegalStateException("IncorrectPhase");

        float minFirePower = Float.MAX_VALUE;

        Player penaltyPlayer = activePlayers.getFirst();

        for (Player player : activePlayers) {
            // Calculates the current player firepower
            float currFirePower = tempFirePower.get(player);

            if (currFirePower < minFirePower) {
                minFirePower = currFirePower;
                penaltyPlayer = player;
            }
            else if (currFirePower == minFirePower) {
                // In case of tie, picks farthest player on the route
                if (player.getPosition() > penaltyPlayer.getPosition()) {
                    penaltyPlayer = player;
                }
            }
        }

        gameManager.getGame().setActivePlayer(penaltyPlayer);

        phase = EventPhase.PENALTY;
        penalty(penaltyPlayer);
    }

    /**
     * Chooses the next phase based on the PenaltyType
     *
     * @author Stefano
     * @param penaltyPlayer the player who is penalized
     * @throws InterruptedException
     */
    private void penalty(Player penaltyPlayer) throws InterruptedException {
        if (!phase.equals(EventPhase.PENALTY))
            throw new IllegalStateException("IncorrectPhase");

        // Notifies penalized player
        Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);
        MessageSenderService.sendMessage("YouArePenalizedPlayer", sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerGotPenalizedMessage(penaltyPlayer.getName()), sender);

        switch (currentPenalty.getPenalty().getType()){

            case PenaltyType.PENALTYDAYS:
                phase = EventPhase.PENALTY_DAYS;
                penaltyDays(penaltyPlayer);
                break;

            case PenaltyType.PENALTYCREW:
                // Request needed amount of crew
                requestedCrew = currentPenalty.getPenalty().getNeededAmount();

                phase = EventPhase.PENALTY_CREW;
                penaltyCrew(penaltyPlayer);
                break;

            case PenaltyType.PENALTYSHOTS:
                // Penalty shots
                penaltyShots = new ArrayList<>(currentPenalty.getPenalty().getShots());

                phase = EventPhase.PENALTY_SHOTS;
                penaltyShot(penaltyPlayer);
                break;

            case PenaltyType.PENALTYBOXES:
                // Number of requested boxes
                requestedBoxes =  currentPenalty.getPenalty().getNeededAmount();

                phase = EventPhase.PENALTY_BOXES;
                penaltyBoxes(penaltyPlayer);
                break;
        }

        // Reset controller temp values
        tempFirePower.clear();
        tempEnginePower.clear();
    }

    /**
     * Player loses penaltyDays
     *
     * @author Stefano
     * @throws InterruptedException
     */
    private void penaltyDays(Player penaltyPlayer) throws InterruptedException {
        if (!phase.equals(EventPhase.PENALTY_DAYS))
            throw new IllegalStateException("IncorrectPhase");

        Board board = gameManager.getGame().getBoard();

        // Gets sender reference related to current player
        Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

        // Event effect applied for single player
        battlezone.penaltyDays(gameManager.getGame().getBoard(), penaltyPlayer, currentPenalty.getPenalty().getNeededAmount());

        MessageSenderService.sendMessage(new PlayerMovedBackwardMessage(currentPenalty.getPenalty().getNeededAmount()), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedBackwardMessage(penaltyPlayer.getName(), currentPenalty.getPenalty().getNeededAmount()), sender);

        // Updates turn order
        board.updateTurnOrder();
    }

    /**
     * Handles crew penalty
     *
     * @author Gabriele
     * @throws InterruptedException
     */
    private void penaltyCrew(Player penaltyPlayer) throws InterruptedException {
        if (!phase.equals(EventPhase.PENALTY_CREW))
            throw new IllegalStateException("IncorrectPhase");

        // Gets penalty player sender reference
        Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

        // Request to discard crew
        phase = EventPhase.DISCARDED_CREW;
        MessageSenderService.sendMessage(new CrewToDiscardMessage(requestedCrew), sender);

        gameManager.getGameThread().resetAndWaitTravelerReady(penaltyPlayer);

        // Update spaceship to remove highlight components
        // For others, it's used to reload the spaceship in case of disconnections while he was discarding.
        gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(penaltyPlayer.getSpaceship(), penaltyPlayer));

        // If the player is disconnected
        if(!penaltyPlayer.getIsReady()) {
            battlezone.randomDiscardCrew(penaltyPlayer.getSpaceship(), requestedCrew);

            // For others, it's used to reload the spaceship in case they got disconnected while it was discarding.
            gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(penaltyPlayer.getSpaceship(), penaltyPlayer));
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
     */
    @Override
    public void receiveDiscardedCrew(Player player, int xHousingUnit, int yHousingUnit, Sender sender) {
        if (!phase.equals(EventPhase.DISCARDED_CREW)) {
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
        if (xHousingUnit < 0 || yHousingUnit < 0 || yHousingUnit >= spaceshipMatrix.length || xHousingUnit >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendMessage("InvalidCoordinates", sender);
            MessageSenderService.sendMessage(new CrewToDiscardMessage(requestedCrew), sender);
            return;
        }

        Component housingUnitComp = spaceshipMatrix[yHousingUnit][xHousingUnit];

        // Checks if component is a housing unit
        if (housingUnitComp == null || (!housingUnitComp.getType().equals(ComponentType.HOUSING_UNIT) && !housingUnitComp.getType().equals(ComponentType.CENTRAL_UNIT))) {
            MessageSenderService.sendMessage("InvalidComponent", sender);
            MessageSenderService.sendMessage(new CrewToDiscardMessage(requestedCrew), sender);
            return;
        }

        HousingUnit housingUnit = (HousingUnit) housingUnitComp;

        if(housingUnit.getCrewCount() == 0) {
            MessageSenderService.sendMessage("EmptyHousingUnit", sender);
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedCrew), sender);
            return;
        }

        battlezone.chooseDiscardedCrew(player.getSpaceship(), housingUnit);
        requestedCrew--;

        MessageSenderService.sendMessage(new CrewDiscardedMessage(xHousingUnit, yHousingUnit), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerCrewDiscardedMessage(player.getName(), xHousingUnit, yHousingUnit), sender);

        if (requestedCrew == 0 || player.getSpaceship().getTotalCrewCount() == 0) {
            player.setIsReady(true);
            gameManager.getGameThread().notifyThread();

        } else {
            MessageSenderService.sendMessage(new CrewToDiscardMessage(requestedCrew), sender);
        }
    }

    /**
     * Handles box penalty
     *
     * @author Gabriele
     * @param penaltyPlayer the player who is penalized
     * @throws InterruptedException
     */
    private void penaltyBoxes(Player penaltyPlayer) throws InterruptedException {
        if (!phase.equals(EventPhase.PENALTY_BOXES))
            throw new IllegalStateException("IncorrectPhase");

        Spaceship spaceship = penaltyPlayer.getSpaceship();

        // Gets penalty player sender reference
        Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

        // Checks if he has at least a box/battery
        if (spaceship.getBoxesCount() == 0 && spaceship.getBatteriesCount() == 0) {
            MessageSenderService.sendMessage("NotEnoughBoxesAndBatteries", sender);
            return;
        }

        // Resets battery storages list and box storages list
        batteryStorages.clear();

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

        gameManager.getGameThread().resetAndWaitTravelerReady(penaltyPlayer);

        // Update spaceship to remove highlight components
        // For others, it's used to reload the spaceship in case of disconnections while he was discarding.
        gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(penaltyPlayer.getSpaceship(), penaltyPlayer));

        // If the penaltyPlayer disconnected
        if(!penaltyPlayer.getIsReady()){

            if(spaceship.getBoxesCount() >= requestedBoxes){
                battlezone.randomDiscardBoxes(spaceship, requestedBoxes);
            }else{

                requestedBatteries = requestedBoxes - spaceship.getBoxesCount();

                battlezone.randomDiscardBoxes(spaceship, spaceship.getBoxesCount());

                battlezone.randomDiscardBatteries(spaceship, requestedBatteries);
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
     * @param sender current sende
     */
    @Override
    public void receiveDiscardedBox(Player player, int xBoxStorage, int yBoxStorage, int idx, Sender sender) {
        Spaceship spaceship = player.getSpaceship();

        if (!phase.equals(EventPhase.DISCARDED_BOXES)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getSpaceshipMatrixCopy();

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

        int[] boxCounts = spaceship.getBoxCounts();

        if(
            (boxCounts[0] != 0 && !box.equals(Box.RED))     ||
            (boxCounts[0] == 0 && boxCounts[1] != 0 && !box.equals(Box.YELLOW))  ||
            (boxCounts[0] == 0 && boxCounts[1] == 0 && boxCounts[2] != 0 && !box.equals(Box.GREEN))   ||
            (boxCounts[0] == 0 && boxCounts[1] == 0 && boxCounts[2] == 0 && boxCounts[3] != 0 && !box.equals(Box.BLUE)))
        {
            MessageSenderService.sendMessage("IsNotMaxValuableBox", sender);
            MessageSenderService.sendMessage(new BoxToDiscardMessage(requestedBoxes), sender);
            return;
        }

        boxStorage.removeBox(spaceship, idx);
        requestedBoxes--;

        MessageSenderService.sendMessage(new BoxDiscardedMessage(xBoxStorage, yBoxStorage, idx), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerBoxDiscardedMessage(player.getName(), xBoxStorage, yBoxStorage, idx), sender);

        if (requestedBoxes == 0) {

            player.setIsReady(true);
            gameManager.getGameThread().notifyThread();

        } else {

            if (spaceship.getBoxesCount() > 0){
                MessageSenderService.sendMessage(new BoxToDiscardMessage(requestedBoxes), sender);

            } else {

                if (spaceship.getBatteriesCount() > 0) {
                    MessageSenderService.sendMessage("NotEnoughBoxes", sender);
                    requestedBatteries = requestedBoxes;
                    phase = EventPhase.DISCARDED_BATTERIES_FOR_BOXES;
                    MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

                } else {
                    MessageSenderService.sendMessage("NotEnoughBatteries", sender);

                    player.setIsReady(true);
                    gameManager.getGameThread().notifyThread();
                }
            }
        }
    }

    /**
     * Handles penalty shots
     *
     * @author Gabriele
     * @throws InterruptedException
     */
    private void penaltyShot(Player penaltyPlayer) throws InterruptedException {
        if (!phase.equals(EventPhase.PENALTY_SHOTS))
            throw new IllegalStateException("IncorrectPhase");

        // Checks if penalty shots are empty
        for (Projectile shot : penaltyShots) {

            currentShot = shot;

            // If penalty player is not on the board anymore skip the remaining shots
            if (!gameManager.getGame().getBoard().getCopyTravelers().contains(penaltyPlayer)) {
                break;
            }

            // Gets penalty player sender reference
            Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

            MessageSenderService.sendMessage(new IncomingProjectileMessage(currentShot), sender);

            phase = EventPhase.ASK_ROLL_DICE;
            askToRollDice(penaltyPlayer);

            // Delay to show the dice result
            gameManager.getGameThread().sleep(3000);

            // Sets penalty player as not ready
            penaltyPlayer.setIsReady(false);

            // Checks if the shot is small or big
            if (currentShot.getSize().equals(ProjectileSize.SMALL)) {
                phase = EventPhase.ASK_SHIELDS;
                askToUseShields(penaltyPlayer);
            } else {
                phase = EventPhase.HANDLE_SHOT;
                handleShot(penaltyPlayer);
            }

            gameManager.getGameThread().waitTravelerReady(penaltyPlayer);

            // If the player is disconnected
            if (!penaltyPlayer.getIsReady()) {
                handleCurrentMeteorForDisconnectedPlayer(penaltyPlayer);
            }

            // Delay to show the dice result
            gameManager.getGameThread().sleep(3000);
        }
    }

    /**
     * Asks first penalized player to roll dice
     *
     * @author Stefano
     * @param penaltyPlayer is the player that needs to roll dice
     */
    private void askToRollDice(Player penaltyPlayer) throws InterruptedException {
        if (!phase.equals(EventPhase.ASK_ROLL_DICE))
            throw new IllegalStateException("IncorrectPhase");

        // Asks penalty player to roll dice
        Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

        phase = EventPhase.ROLL_DICE;

        if (currentShot.getFrom() == 0 || currentShot.getFrom() == 2) {
            MessageSenderService.sendMessage("RollDiceToFindColumn", sender);

        } else if (currentShot.getFrom() == 1 || currentShot.getFrom() == 3) {
            MessageSenderService.sendMessage("RollDiceToFindRow", sender);
        }

        gameManager.getGameThread().resetAndWaitTravelerReady(penaltyPlayer);

        // If the player is disconnected
        if(!penaltyPlayer.getIsReady()) {
            rollDice(penaltyPlayer, null);
        }
    }

    /**
     * Rolls dice and collects the result
     *
     * @author Stefano
     * @param player current player
     * @param sender current sender
     */
    @Override
    public void rollDice(Player player, Sender sender){
        if (!phase.equals(EventPhase.ROLL_DICE)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the defeated player
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        diceResult = player.rollDice();

        MessageSenderService.sendMessage(new DiceResultMessage(diceResult), sender);

        player.setIsReady(true);
        gameManager.getGameThread().notifyThread();
    }

    /**
     * Asks penalty players if they want to use shields to protect
     *
     * @author Stefano
     * @param penaltyPlayer is the player that needs to handle the current shot
     */
    private void askToUseShields(Player penaltyPlayer){
        if (!phase.equals(EventPhase.ASK_SHIELDS))
            throw new IllegalStateException("IncorrectPhase");

        // Asks penalty player if he wants to use a shield
        Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

        // Finds impact component
        Component affectedComponent = battlezone.penaltyShot(gameManager.getGame(), penaltyPlayer, currentShot, diceResult);

        // Checks if there is any affected component
        if (affectedComponent == null) {
            MessageSenderService.sendMessage("NoComponentHit", sender);

            penaltyPlayer.setIsReady(true);
            gameManager.getGameThread().notifyThread();
            return;
        }

        MessageSenderService.sendMessage(new AffectedComponentMessage(affectedComponent.getX(), affectedComponent.getY()), sender);

        // Checks if penalty player has a shield that covers that direction
        boolean hasShield = battlezone.checkShields(penaltyPlayer, currentShot);

        if (hasShield && penaltyPlayer.getSpaceship().getBatteriesCount() > 0) {
            phase = EventPhase.SHIELD_DECISION;
            MessageSenderService.sendMessage("AskToUseShield", sender);

        } else {
            MessageSenderService.sendMessage("NoShieldAvailable", sender);
            phase = EventPhase.HANDLE_SHOT;
            handleShot(penaltyPlayer);
        }
    }

    /**
     * Receives shield decision
     *
     * @author Gabriele
     * @param player current player
     * @param response player's response
     * @param sender current sender
     */
    @Override
    public void receiveProtectionDecision(Player player, String response, Sender sender){
        if (!phase.equals(EventPhase.SHIELD_DECISION)) {
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
                phase = EventPhase.SHIELD_BATTERY;
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(1), sender);
                break;

            case "NO":
                phase = EventPhase.HANDLE_SHOT;
                handleShot(player);
                break;

            default:
                MessageSenderService.sendMessage("IncorrectResponse", sender);
                MessageSenderService.sendMessage("AskToUseShield", sender);
                break;
        }
    }

    /**
     * Handles current shot
     *
     * @author Stefano
     * @param penaltyPlayer is the player that needs to handle the current shot
     */
    private void handleShot(Player penaltyPlayer) {
        if (!phase.equals(EventPhase.HANDLE_SHOT))
            throw new IllegalStateException("IncorrectPhase");

        Game game = gameManager.getGame();

        Component destroyedComponent = battlezone.penaltyShot(game, penaltyPlayer, currentShot, diceResult);

        // Gets penalty player sender reference
        Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

        // Sends two types of messages based on the shot's result
        if (destroyedComponent == null) {
            MessageSenderService.sendMessage("NothingGotDestroyed", sender);

            penaltyPlayer.setIsReady(true);
            gameManager.getGameThread().notifyThread();
            return;
        }

        if (SpaceshipController.destroyComponentAndCheckValidity(gameManager, penaltyPlayer, destroyedComponent.getX(), destroyedComponent.getY(), sender)){
            penaltyPlayer.setIsReady(true);
            gameManager.getGameThread().notifyThread();

        } else
            MessageSenderService.sendMessage("AskSelectSpaceshipPart", sender);
    }

    /**
     * Handles current meteor for disconnected players
     *
     * @author Alessandro
     * @param player is the one that needs to handle the current meteor
     */
    private void handleCurrentMeteorForDisconnectedPlayer(Player player) {
        Game game = gameManager.getGame();

        // Handles current shot for player
        Component affectedComponent = battlezone.penaltyShot(game, player, currentShot, diceResult);

        if(affectedComponent == null)
            return;

        // Gets current player sender reference
        Sender sender = gameManager.getSenderByPlayer(player);

        // Destroys affected component
        if(!SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, affectedComponent.getX(), affectedComponent.getY(), sender)){
            gameManager.getGame().getBoard().leaveTravel(player);
            gameManager.broadcastGameMessage(new UpdateOtherTravelersShipMessage(gameManager.getGame().getBoard().getCopyTravelers()));
            gameManager.broadcastGameMessage(new UpdateTrackMessage(GameController.getAllPlayersInTrackCopy(gameManager), gameManager.getGame().getBoard().getTrack()));
        }
    }

    /**
     * Reconnects player to the game
     *
     * @author Alessandro
     * @param player current player
     * @param sender current sender
     */
    @Override
    public void reconnectPlayer(Player player, Sender sender) {
        if(!player.equals(gameManager.getGame().getActivePlayer()))
            return;

        if (phase.equals(EventPhase.ENGINE_NUMBER)){
            int maxUsable = player.getSpaceship().maxNumberOfDoubleEnginesUsable();
            MessageSenderService.sendMessage(new HowManyDoubleEnginesMessage(maxUsable, player.getSpaceship().getNormalEnginePower()), sender);
        }
        else if (phase.equals(EventPhase.CANNON_NUMBER)){
            int maxUsable = player.getSpaceship().maxNumberOfDoubleCannonsUsable();
            MessageSenderService.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, 0, player.getSpaceship().getNormalShootingPower()), sender);
        }
        else if (phase.equals(EventPhase.DISCARDED_BOXES)){
            MessageSenderService.sendMessage(new BoxToDiscardMessage(requestedBoxes), sender);
        }
        else if (phase.equals(EventPhase.DISCARDED_BATTERIES) || phase.equals(EventPhase.DISCARDED_BATTERIES_FOR_BOXES)){

            // Remove batteries already discarded (DISCARDED_BATTERIES)
            for(BatteryStorage batteryStorage : batteryStorages){
                MessageSenderService.sendMessage(new BatteryDiscardedMessage(batteryStorage.getX(), batteryStorage.getY()), sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), batteryStorage.getX(), batteryStorage.getY()), sender);
            }

            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);
        }
        else if (phase.equals(EventPhase.DISCARDED_CREW)){
            MessageSenderService.sendMessage(new CrewToDiscardMessage(requestedCrew), sender);
        }
        else if (phase.equals(EventPhase.ROLL_DICE)){
            MessageSenderService.sendMessage(new IncomingProjectileMessage(currentShot), sender);

            if (currentShot.getFrom() == 0 || currentShot.getFrom() == 2) {
                MessageSenderService.sendMessage("RollDiceToFindColumn", sender);

            } else if (currentShot.getFrom() == 1 || currentShot.getFrom() == 3) {
                MessageSenderService.sendMessage("RollDiceToFindRow", sender);
            }
        }
        else if (phase.equals(EventPhase.SHIELD_DECISION)){
            MessageSenderService.sendMessage(new IncomingProjectileMessage(currentShot), sender);
            MessageSenderService.sendMessage("AskToUseShield", sender);

        }
        else if (phase.equals(EventPhase.SHIELD_BATTERY)) {
            MessageSenderService.sendMessage(new IncomingProjectileMessage(currentShot), sender);
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(1), sender);
        }
        else if(phase.equals(EventPhase.HANDLE_SHOT) && !player.getSpaceship().getBuildingBoard().checkShipValidityAndFixAliens()){
            MessageSenderService.sendMessage("AskSelectSpaceshipPart", sender);
        }
    }
}