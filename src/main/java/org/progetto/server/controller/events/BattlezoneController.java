package org.progetto.server.controller.events;

import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.AffectedComponentMessage;
import org.progetto.messages.toClient.Battlezone.AnotherPlayerGotPenalizedMessage;
import org.progetto.messages.toClient.Battlezone.EvaluatingConditionMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.controller.SpaceshipController;
import org.progetto.server.model.Board;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.*;

import java.rmi.RemoteException;
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
    private ArrayList<Projectile> penaltyShots;
    private Projectile currentShot;
    private final Map<Player, Integer> tempEnginePower;
    private final Map<Player, Float> tempFirePower;
    private Player penaltyPlayer;
    private int requestedBatteries;
    private int requestedCrew;
    private int requestedBoxes;
    private int diceResult;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BattlezoneController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.battlezone = (Battlezone) gameManager.getGame().getActiveEventCard();
        this.phase = EventPhase.START;
        this.couples = new ArrayList<>(this.battlezone.getCouples());
        this.penaltyShots = new ArrayList<>();
        this.currentShot = null;
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();
        this.tempEnginePower = new HashMap<>();
        this.tempFirePower = new HashMap<>();
        this.penaltyPlayer = null;
        this.requestedBatteries = 0;
        this.requestedCrew = 0;
        this.requestedBoxes = 0;
        this.diceResult = 0;
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
        if (!couples.isEmpty()) {

            switch (couples.getFirst().getCondition()){

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
        penaltyPlayer = battlezone.lessPopulatedSpaceship(activePlayers);

        phase = EventPhase.PENALTY;
        penalty();
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

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            // Calculates max number of double engine usable
            int maxUsable = player.getSpaceship().maxNumberOfDoubleEnginesUsable();

            // If he can't use any double engine, apply event effect; otherwise, ask how many he wants to use
            if (maxUsable == 0) {
                tempEnginePower.put(player, player.getSpaceship().getNormalEnginePower());

            } else {
                MessageSenderService.sendOptional(new HowManyDoubleEnginesMessage(maxUsable, player.getSpaceship().getNormalEnginePower()), sender);
                phase = EventPhase.ENGINE_NUMBER;

                gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                gameManager.getGameThread().resetAndWaitTravelerReady(player);
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
            return;
        }

        // Player doesn't want to use double engines
        if (num == 0) {
            tempEnginePower.put(player, player.getSpaceship().getNormalEnginePower());

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else if (num <= player.getSpaceship().getDoubleEngineCount() && num <= player.getSpaceship().getBatteriesCount() && num > 0) {
            requestedBatteries = num;

            tempEnginePower.put(player, player.getSpaceship().getNormalEnginePower() + 2 * num);

            MessageSenderService.sendOptional(new BatteriesToDiscardMessage(num), sender);
            phase = EventPhase.DISCARDED_BATTERIES;

        } else {
            MessageSenderService.sendOptional("IncorrectNumber", sender);
            int maxUsable = player.getSpaceship().maxNumberOfDoubleEnginesUsable();
            MessageSenderService.sendOptional(new HowManyDoubleEnginesMessage(maxUsable, player.getSpaceship().getNormalEnginePower()), sender);
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

            Spaceship spaceship = player.getSpaceship();

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            // Calculates max number of double cannons usable
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

            // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
            if (maxUsable == 0) {
                tempFirePower.put(player, player.getSpaceship().getNormalShootingPower());

            } else {
                MessageSenderService.sendOptional(new HowManyDoubleCannonsMessage(maxUsable, 0, player.getSpaceship().getNormalShootingPower()), sender);
                phase = EventPhase.CANNON_NUMBER;

                gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                gameManager.getGameThread().resetAndWaitTravelerReady(player);
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
            tempFirePower.put(player, player.getSpaceship().getNormalShootingPower());
            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else if (num <= (spaceship.getFullDoubleCannonCount() + spaceship.getHalfDoubleCannonCount()) && num <= player.getSpaceship().getBatteriesCount() && num > 0) {
            requestedBatteries = num;

            // Updates player's firepower based on his decision
            if (num <= spaceship.getFullDoubleCannonCount()) {
                tempFirePower.put(player, spaceship.getNormalShootingPower() + 2 * num);
            } else {
                tempFirePower.put(player, spaceship.getNormalShootingPower() + 2 * spaceship.getFullDoubleCannonCount() + (num - spaceship.getFullDoubleCannonCount()));
            }

            MessageSenderService.sendOptional(new BatteriesToDiscardMessage(num), sender);
            phase = EventPhase.DISCARDED_BATTERIES;

        } else {
            MessageSenderService.sendOptional("IncorrectNumber", sender);
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();
            MessageSenderService.sendOptional(new HowManyDoubleCannonsMessage(maxUsable, 0, player.getSpaceship().getNormalShootingPower()), sender);
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
        switch (phase) {
            case DISCARDED_BATTERIES:
                if (!player.equals(gameManager.getGame().getActivePlayer())) {
                    MessageSenderService.sendOptional("NotYourTurn", sender);
                    return;
                }
                break;

            case DISCARDED_BATTERIES_FOR_BOXES:
                if (!player.equals(penaltyPlayer)) {
                    MessageSenderService.sendOptional("NotYourTurn", sender);
                    return;
                }
                break;

            case SHIELD_BATTERY:
                if (!player.equals(penaltyPlayer)) {
                    MessageSenderService.sendOptional("NotYourTurn", sender);
                    return;
                }
                break;

            default:
                MessageSenderService.sendOptional("IncorrectPhase", sender);
                return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xBatteryStorage < 0 || yBatteryStorage < 0 || yBatteryStorage >= spaceshipMatrix.length || xBatteryStorage >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendOptional("InvalidCoordinates", sender);

            if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBatteries), sender);

            } else if (phase.equals(EventPhase.DISCARDED_BATTERIES_FOR_BOXES)) {
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBoxes), sender);

            } else if (phase.equals(EventPhase.SHIELD_BATTERY)) {
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(1), sender);
            }
            return;
        }

        Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

        // Checks if component is a battery storage
        if (batteryStorage == null || !batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {
            MessageSenderService.sendOptional("InvalidComponent", sender);

            if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBatteries), sender);

            } else if (phase.equals(EventPhase.DISCARDED_BATTERIES_FOR_BOXES)) {
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBoxes), sender);

            } else if (phase.equals(EventPhase.SHIELD_BATTERY)) {
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(1), sender);
            }
            return;
        }

        if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {

            // Checks if a battery has been discarded
            if (battlezone.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                requestedBatteries--;

                MessageSenderService.sendOptional(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

                if (requestedBatteries == 0) {

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();

                } else {
                    MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBatteries), sender);
                }

            } else {
                MessageSenderService.sendOptional("BatteryNotDiscarded", sender);
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBatteries), sender);
            }

        } else if (phase.equals(EventPhase.DISCARDED_BATTERIES_FOR_BOXES)) {

            // Checks if a battery has been discarded
            if (battlezone.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                requestedBoxes--;

                MessageSenderService.sendOptional(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

                if (requestedBoxes == 0) {

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();

                } else {

                    // Checks if he has at least a battery to discard
                    if (player.getSpaceship().getBatteriesCount() > 0) {
                        MessageSenderService.sendOptional("NotEnoughBoxes", sender);
                        MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBoxes), sender);
                        phase = EventPhase.DISCARDED_BATTERIES_FOR_BOXES;

                    } else {
                        MessageSenderService.sendOptional("NotEnoughBatteries", sender);

                        player.setIsReady(true, gameManager.getGame());
                        gameManager.getGameThread().notifyThread();
                    }
                }

            } else {
                MessageSenderService.sendOptional("BatteryNotDiscarded", sender);
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBoxes), sender);
            }

        } else if (phase.equals(EventPhase.SHIELD_BATTERY)) {

            // Checks if a battery has been discarded
            if (battlezone.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {

                MessageSenderService.sendOptional(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

                phase = EventPhase.HANDLE_SHOT;
                gameManager.broadcastGameMessage("NothingGotDestroyed");

                penaltyPlayer.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();

            } else {
                MessageSenderService.sendOptional("BatteryNotDiscarded", sender);
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(1), sender);
            }
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

        phase = EventPhase.PENALTY;
        penalty();
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

        phase = EventPhase.PENALTY;
        penalty();
    }

    /**
     * Chooses the next phase based on the PenaltyType
     *
     * @author Stefano
     * @throws InterruptedException
     */
    private void penalty() throws InterruptedException {
        if (!phase.equals(EventPhase.PENALTY))
            throw new IllegalStateException("IncorrectPhase");

        // Notifies penalized player
        Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);
        MessageSenderService.sendOptional("YouArePenalizedPlayer", sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerGotPenalizedMessage(penaltyPlayer.getName()), sender);

        switch (couples.getFirst().getPenalty().getType()){

            case PenaltyType.PENALTYDAYS:
                phase = EventPhase.PENALTY_DAYS;
                penaltyDays();
                break;

            case PenaltyType.PENALTYCREW:
                // Request needed amount of crew
                requestedCrew = couples.getFirst().getPenalty().getNeededAmount();

                phase = EventPhase.PENALTY_CREW;
                penaltyCrew();
                break;

            case PenaltyType.PENALTYSHOTS:
                // Penalty shots
                penaltyShots = new ArrayList<>(couples.getFirst().getPenalty().getShots());

                phase = EventPhase.PENALTY_SHOTS;
                penaltyShot();
                break;

            case PenaltyType.PENALTYBOXES:
                // Number of requested boxes
                requestedBoxes =  couples.getFirst().getPenalty().getNeededAmount();

                phase = EventPhase.PENALTY_BOXES;
                penaltyBoxes();
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
    private void penaltyDays() throws InterruptedException {
        if (!phase.equals(EventPhase.PENALTY_DAYS))
            throw new IllegalStateException("IncorrectPhase");

        Board board = gameManager.getGame().getBoard();

        // Gets sender reference related to current player
        Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

        // Event effect applied for single player
        battlezone.penaltyDays(gameManager.getGame().getBoard(), penaltyPlayer, couples.getFirst().getPenalty().getNeededAmount());

        MessageSenderService.sendOptional(new PlayerMovedBackwardMessage(couples.getFirst().getPenalty().getNeededAmount()), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedBackwardMessage(penaltyPlayer.getName(), couples.getFirst().getPenalty().getNeededAmount()), sender);

        // Updates turn order
        board.updateTurnOrder();

        // Next Couple
        couples.removeFirst();
        phase = EventPhase.CONDITION;
        condition();
    }

    /**
     * Handles crew penalty
     *
     * @author Gabriele
     * @throws InterruptedException
     */
    private void penaltyCrew() throws InterruptedException {
        if (!phase.equals(EventPhase.PENALTY_CREW))
            throw new IllegalStateException("IncorrectPhase");

        // Gets penalty player sender reference
        Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

        // Request to discard crew
        MessageSenderService.sendOptional(new CrewToDiscardMessage(requestedCrew), sender);
        phase = EventPhase.DISCARDED_CREW;

        gameManager.getGameThread().resetAndWaitTravelerReady(penaltyPlayer);

        // Next Couple
        couples.removeFirst();
        phase = EventPhase.CONDITION;
        condition();
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
        if (!player.equals(penaltyPlayer)) {
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

        Component housingUnit = spaceshipMatrix[yHousingUnit][xHousingUnit];

        // Checks if component is a housing unit
        if (housingUnit == null || (!housingUnit.getType().equals(ComponentType.HOUSING_UNIT) && !housingUnit.getType().equals(ComponentType.CENTRAL_UNIT))) {
            MessageSenderService.sendOptional("InvalidComponent", sender);
            MessageSenderService.sendOptional(new CrewToDiscardMessage(requestedCrew), sender);
            return;
        }

        // Checks if a crew member has been discarded
        try {
            battlezone.chooseDiscardedCrew(player.getSpaceship(), (HousingUnit) housingUnit);
            requestedCrew--;

            MessageSenderService.sendOptional(new CrewDiscardedMessage(xHousingUnit, yHousingUnit), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerCrewDiscardedMessage(player.getName(), xHousingUnit, yHousingUnit), sender);

            if (requestedCrew == 0 || player.getSpaceship().getTotalCrewCount() == 0) {

                player.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();
            } else
                MessageSenderService.sendOptional(new CrewToDiscardMessage(requestedCrew), sender);

        }catch (IllegalStateException e) {
            MessageSenderService.sendOptional("CrewMemberNotDiscarded", sender);
            MessageSenderService.sendOptional(new CrewToDiscardMessage(requestedCrew), sender);
        }
    }

    /**
     * Handles box penalty
     *
     * @author Gabriele
     * @throws InterruptedException
     */
    private void penaltyBoxes() throws InterruptedException {
        if (!phase.equals(EventPhase.PENALTY_BOXES))
            throw new IllegalStateException("IncorrectPhase");

        Player player = penaltyPlayer;

        // Gets penalty player sender reference
        Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

        // Box currently owned
        int boxCount = player.getSpaceship().getBoxCounts()[0] + player.getSpaceship().getBoxCounts()[1] + player.getSpaceship().getBoxCounts()[2] + player.getSpaceship().getBoxCounts()[3];

        // Checks if he has at least a box to discard
        if (boxCount > 0) {
            MessageSenderService.sendOptional(new BoxToDiscardMessage(requestedBoxes), sender);
            phase = EventPhase.DISCARDED_BOXES;

            gameManager.getGameThread().resetAndWaitTravelerReady(penaltyPlayer);

        } else {

            // Checks if he has at least a battery to discard
            if (player.getSpaceship().getBatteriesCount() > 0) {
                MessageSenderService.sendOptional("NotEnoughBoxes", sender);
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBoxes), sender);
                phase = EventPhase.DISCARDED_BATTERIES_FOR_BOXES;

                gameManager.getGameThread().resetAndWaitTravelerReady(penaltyPlayer);

            } else {
                MessageSenderService.sendOptional("NotEnoughBatteries", sender);
            }
        }

        // Next Couple
        couples.removeFirst();
        phase = EventPhase.CONDITION;
        condition();
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
        if (!phase.equals(EventPhase.DISCARDED_BOXES)) {
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(penaltyPlayer)) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xBoxStorage < 0 || yBoxStorage < 0 || yBoxStorage >= spaceshipMatrix.length || xBoxStorage >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendOptional("InvalidCoordinates", sender);
            MessageSenderService.sendOptional(new BoxToDiscardMessage(requestedBoxes), sender);
            return;
        }

        Component boxStorage = spaceshipMatrix[yBoxStorage][xBoxStorage];

        // Checks if component is a box storage
        if (boxStorage == null || (!boxStorage.getType().equals(ComponentType.BOX_STORAGE) && !boxStorage.getType().equals(ComponentType.RED_BOX_STORAGE))) {
            MessageSenderService.sendOptional("InvalidComponent", sender);
            MessageSenderService.sendOptional(new BoxToDiscardMessage(requestedBoxes), sender);
            return;
        }

        // Checks if a box has been discarded
        if (battlezone.chooseDiscardedBox(player.getSpaceship(), (BoxStorage) boxStorage, idx)) {
            requestedBoxes--;

            MessageSenderService.sendOptional(new BoxDiscardedMessage(xBoxStorage, yBoxStorage, idx), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerBoxDiscardedMessage(player.getName(), xBoxStorage, yBoxStorage, idx), sender);

            if (requestedBoxes == 0) {
                player.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();

            } else {

                // Box currently owned
                int boxCount = player.getSpaceship().getBoxCounts()[0] + player.getSpaceship().getBoxCounts()[1] + player.getSpaceship().getBoxCounts()[2] + player.getSpaceship().getBoxCounts()[3];

                if (boxCount > 0) {
                    MessageSenderService.sendOptional(new BoxToDiscardMessage(requestedBoxes), sender);
                    phase = EventPhase.DISCARDED_BOXES;

                } else {

                    // Checks if he has at least a battery to discard
                    if (player.getSpaceship().getBatteriesCount() > 0) {
                        MessageSenderService.sendOptional("NotEnoughBoxes", sender);
                        MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBoxes), sender);
                        phase = EventPhase.DISCARDED_BATTERIES_FOR_BOXES;

                    } else {
                        MessageSenderService.sendOptional("NotEnoughBatteries", sender);

                        player.setIsReady(true, gameManager.getGame());
                        gameManager.getGameThread().notifyThread();
                    }
                }
            }

        } else {
            MessageSenderService.sendOptional("BoxNotDiscarded", sender);
            MessageSenderService.sendOptional(new BoxToDiscardMessage(requestedBoxes), sender);
        }
    }

    /**
     * Handles penalty shots
     *
     * @author Gabriele
     * @throws InterruptedException
     */
    private void penaltyShot() throws InterruptedException {
        if (!phase.equals(EventPhase.PENALTY_SHOTS))
            throw new IllegalStateException("IncorrectPhase");

        // Checks if penalty shots are empty
        for (Projectile shot : penaltyShots) {

            currentShot = shot;

            // Gets penalty player sender reference
            Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

            MessageSenderService.sendOptional(new IncomingProjectileMessage(currentShot), sender);

            phase = EventPhase.ASK_ROLL_DICE;
            askToRollDice();

            gameManager.getGameThread().resetAndWaitTravelerReady(penaltyPlayer);
        }

        // Next Couple
        couples.removeFirst();
        phase = EventPhase.CONDITION;
        condition();
    }

    /**
     * Asks first penalized player to roll dice
     *
     * @author Stefan
     */
    private void askToRollDice(){
        if (!phase.equals(EventPhase.ASK_ROLL_DICE))
            throw new IllegalStateException("IncorrectPhase");

        // Asks penalty player to roll dice
        Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

        if (currentShot.getFrom() == 0 || currentShot.getFrom() == 2) {
            MessageSenderService.sendOptional("RollDiceToFindColumn", sender);

        } else if (currentShot.getFrom() == 1 || currentShot.getFrom() == 3) {
            MessageSenderService.sendOptional("RollDiceToFindRow", sender);
        }

        phase = EventPhase.ROLL_DICE;
    }

    /**
     * Rolls dice and collects the result
     *
     * @author Stefano
     * @param player current player
     * @param sender current sende
     */
    @Override
    public void rollDice(Player player, Sender sender){
        if (!phase.equals(EventPhase.ROLL_DICE)) {
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the defeated player
        if (!player.equals(penaltyPlayer)) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
            return;
        }

        diceResult = player.rollDice();

        MessageSenderService.sendOptional(new DiceResultMessage(diceResult), sender);

        // Delay to show the dice result
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (currentShot.getSize().equals(ProjectileSize.SMALL)) {
            phase = EventPhase.ASK_SHIELDS;
            askToUseShields();

        } else {
            phase = EventPhase.HANDLE_SHOT;
            handleShot();
        }
    }

    /**
     * Asks penalty players if they want to use shields to protect
     *
     * @author Stefano
     */
    private void askToUseShields(){
        if (!phase.equals(EventPhase.ASK_SHIELDS))
            throw new IllegalStateException("IncorrectPhase");

        // Asks penalty player if he wants to use a shield
        Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

        // Finds impact component
        Component affectedComponent = battlezone.penaltyShot(gameManager.getGame(), penaltyPlayer, currentShot, diceResult);

        // Checks if there is any affected component
        if (affectedComponent == null) {
            MessageSenderService.sendOptional("NoComponentHit", sender);

            penaltyPlayer.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
            return;
        }

        MessageSenderService.sendOptional(new AffectedComponentMessage(affectedComponent.getX(), affectedComponent.getY()), sender);

        // Checks if penalty player has a shield that covers that direction
        boolean hasShield = battlezone.checkShields(penaltyPlayer, currentShot);

        if (hasShield && penaltyPlayer.getSpaceship().getBatteriesCount() > 0) {
            MessageSenderService.sendOptional("AskToUseShield", sender);
            phase = EventPhase.SHIELD_DECISION;

        } else {
            MessageSenderService.sendOptional("NoShieldAvailable", sender);
            phase = EventPhase.HANDLE_SHOT;
            handleShot();
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if (!player.equals(penaltyPlayer)) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
            return;
        }

        String upperCaseResponse = response.toUpperCase();

        switch (upperCaseResponse) {
            case "YES":
                phase = EventPhase.SHIELD_BATTERY;
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(1), sender);
                break;

            case "NO":
                phase = EventPhase.HANDLE_SHOT;
                handleShot();
                break;

            default:
                MessageSenderService.sendOptional("IncorrectResponse", sender);
                MessageSenderService.sendOptional("AskToUseShield", sender);
                break;
        }
    }

    /**
     * Handles current shot
     *
     * @author Stefan
     */
    private void handleShot(){
        if (!phase.equals(EventPhase.HANDLE_SHOT))
            throw new IllegalStateException("IncorrectPhase");

        Game game = gameManager.getGame();

        Component destroyedComponent = battlezone.penaltyShot(game, penaltyPlayer, currentShot, diceResult);

        // Gets penalty player sender reference
        Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

        // Sends two types of messages based on the shot's result
        if (destroyedComponent == null) {
            MessageSenderService.sendOptional("NothingGotDestroyed", sender);
            penaltyPlayer.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
            return;
        }

        if (SpaceshipController.destroyComponentAndCheckValidity(gameManager, penaltyPlayer, destroyedComponent.getX(), destroyedComponent.getY(), sender)){
            penaltyPlayer.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else
            MessageSenderService.sendOptional("AskSelectSpaceshipPart", sender);
    }
}