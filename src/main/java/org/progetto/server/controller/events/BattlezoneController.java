package org.progetto.server.controller.events;

import org.progetto.messages.toClient.EventCommon.*;
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

    private Battlezone battlezone;
    private ArrayList<Player> activePlayers;
    private ArrayList<ConditionPenalty> couples;
    private ArrayList<Projectile> penaltyShots;
    private Map<Player, Integer> tempEnginePower;
    private Map<Player, Float> tempFirePower;
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
        this.couples = new ArrayList<>();
        this.penaltyShots = new ArrayList<>();
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
     * @throws RemoteException
     * @throws InterruptedException
     */
    @Override
    public void start() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.START)) {
            phase = EventPhase.CONDITION;
            condition();
        }
    }

    /**
     * Handles current condition
     *
     * @author Stefano
     * @throws RemoteException
     * @throws InterruptedException
     */
    private void condition() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.CONDITION)) {

            // Checks if all the couples were evaluated
            if (!couples.isEmpty()) {

                switch (couples.getFirst().getCondition()){

                    case ConditionType.CREWREQUIREMENT:
                        phase = EventPhase.CREW_COUNT;
                        chooseFewerCrew();
                        break;

                    case ConditionType.FIREPOWERREQUIREMENT:
                        phase = EventPhase.ASK_ENGINES;
                        askHowManyEnginesToUse();
                        break;

                    case ConditionType.ENGINEPOWERREQUIREMENT:
                        phase = EventPhase.ASK_CANNONS;
                        askHowManyCannonsToUse();
                        break;
                }
            }
        }
    }

    /**
     * Finds player with fewer crew
     *
     * @author Gabriele
     * @throws RemoteException
     * @throws InterruptedException
     */
    private void chooseFewerCrew() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.CREW_COUNT)) {

            // Finds player with fewer crew
            penaltyPlayer = battlezone.lessPopulatedSpaceship(activePlayers);

            phase = EventPhase.PENALTY;
            penalty();
        }
    }

    /**
     * Asks current player how many double engines he wants to use
     *
     * @author Stefano
     * @throws RemoteException
     * @throws InterruptedException
     */
    private void askHowManyEnginesToUse() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.ASK_ENGINES)) {

            for (Player player : activePlayers) {

                gameManager.getGame().setActivePlayer(player);

                Spaceship spaceship = player.getSpaceship();

                // Retrieves sender reference
                Sender sender = gameManager.getSenderByPlayer(player);

                // Calculates max number of double engine usable
                int maxUsable = player.getSpaceship().maxNumberOfDoubleEnginesUsable();

                // If he can't use any double engine, apply event effect; otherwise, ask how many he wants to use
                if (maxUsable == 0) {
                    tempEnginePower.put(player, player.getSpaceship().getNormalEnginePower());

                } else {
                    sender.sendMessage(new HowManyDoubleEnginesMessage(maxUsable));
                    phase = EventPhase.ENGINE_NUMBER;
                }

                gameManager.getGameThread().resetAndWaitPlayerReady(player);
            }

            phase = EventPhase.CHOOSE_WEAKEST_ENGINES;
            chooseWeakestEngines();
        }
    }

    /**
     * Receives numbers of double engines to use
     *
     * @author Stefano
     * @param player current player
     * @param num number of double engines player want to use
     * @param sender current sender
     * @throws RemoteException
     */
    @Override
    public void receiveHowManyEnginesToUse(Player player, int num, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.ENGINE_NUMBER)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                // Player doesn't want to use double engines
                if (num == 0) {
                    tempEnginePower.put(player, player.getSpaceship().getNormalEnginePower());

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();

                } else if (num <= player.getSpaceship().getDoubleEngineCount() && num <= player.getSpaceship().getBatteriesCount() && num > 0) {
                    requestedBatteries = num;
                    tempEnginePower.put(player, player.getSpaceship().getNormalEnginePower() + 2 * num);

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

                // Calculates max number of double cannons usable
                int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

                // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
                if (maxUsable == 0) {
                    tempFirePower.put(player, player.getSpaceship().getNormalShootingPower());

                } else {
                    sender.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, 0));
                    phase = EventPhase.CANNON_NUMBER;
                }

                gameManager.getGameThread().resetAndWaitPlayerReady(player);
            }

            phase = EventPhase.CHOOSE_WEAKEST_CANNONS;
            chooseWeakestCannons();
        }
    }

    /**
     * Receives numbers of double cannons to use
     *
     * @author Stefano
     * @param player current player
     * @param num number of cannon player want to use
     * @param sender current sender
     * @throws RemoteException
     */
    @Override
    public void receiveHowManyCannonsToUse(Player player, int num, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.CANNON_NUMBER)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

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
     * @param player current player
     * @param xBatteryStorage x coordinate of chosen battery storage
     * @param yBatteryStorage y coordinate of chosen battery storage
     * @param sender current sender
     * @throws RemoteException
     */
    @Override
    public void receiveDiscardedBatteries(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();
                Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

                if (batteryStorage != null && batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {

                    // Checks if a battery has been discarded
                    if (battlezone.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                        requestedBatteries--;
                        sender.sendMessage("BatteryDiscarded");

                        if (requestedBatteries == 0) {

                            player.setIsReady(true, gameManager.getGame());
                            gameManager.getGameThread().notifyThread();

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

        } else if (phase.equals(EventPhase.DISCARDED_BATTERIES_FOR_BOXES)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(penaltyPlayer)) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();
                Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

                if (batteryStorage != null && batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {

                    // Checks if a battery has been discarded
                    if (battlezone.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                        requestedBoxes--;
                        sender.sendMessage("BatteryDiscarded");

                        if (requestedBoxes == 0) {

                            player.setIsReady(true, gameManager.getGame());
                            gameManager.getGameThread().notifyThread();

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

                    } else {
                        sender.sendMessage("NotEnoughBatteries");
                    }

                } else {
                    sender.sendMessage("InvalidCoordinates");
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else if (phase.equals(EventPhase.SHIELD_BATTERY)) {

            // Checks if the player that calls the methods has to discard a battery to activate a shield
            if (player.equals(penaltyPlayer)) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();
                Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

                if (batteryStorage != null && batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {

                    // Checks if a battery has been discarded
                    if (battlezone.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {

                        sender.sendMessage("BatteryDiscarded");

                        phase = EventPhase.HANDLE_SHOT;
                        handleShot();

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
     * Finds player with less engine power
     *
     * @author Gabriele
     * @throws RemoteException
     * @throws InterruptedException
     */
    private void chooseWeakestEngines() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.CHOOSE_WEAKEST_ENGINES)) {
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
    }

    /**
     * Finds player with less firepower
     *
     * @author Gabriele
     * @throws RemoteException
     * @throws InterruptedException
     */
    private void chooseWeakestCannons() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.CHOOSE_WEAKEST_CANNONS)) {
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
    }

    /**
     * Chooses the next phase based on the PenaltyType
     *
     * @author Stefano
     * @throws RemoteException
     * @throws InterruptedException
     */
    public void penalty() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.PENALTY)) {

            switch (couples.getFirst().getPenalty().getType()){

                case PenaltyType.PENALTYDAYS:
                    phase = EventPhase.PENALTY_DAYS;
                    penaltyDays();
                    break;

                case PenaltyType.PENALTYCREW:
                    // Request needed amount of crew
                    requestedCrew = couples.getFirst().getPenalty().getNeededAmount();
                    penaltyCrew();

                    phase = EventPhase.PENALTY_CREW;
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
        }

        // Reset controller temp values
        tempFirePower.clear();
        tempEnginePower.clear();
    }

    /**
     * Player loses penaltyDays
     *
     * @author Stefano
     * @throws RemoteException
     * @throws InterruptedException
     */
    private void penaltyDays() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.PENALTY_DAYS)) {

            Board board = gameManager.getGame().getBoard();

            // Gets sender reference related to current player
            Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

            // Event effect applied for single player
            battlezone.penaltyDays(gameManager.getGame().getBoard(), penaltyPlayer, couples.getFirst().getPenalty().getNeededAmount());

            sender.sendMessage(new PlayerMovedBackwardMessage(couples.getFirst().getPenalty().getNeededAmount()));
            gameManager.broadcastGameMessage(new AnotherPlayerMovedBackwardMessage(penaltyPlayer.getName(), couples.getFirst().getPenalty().getNeededAmount()));

            // Updates turn order
            board.updateTurnOrder();

            // Next Couple
            couples.removeFirst();
            phase = EventPhase.CONDITION;
            condition();
        }
    }

    /**
     * Handles crew penalty
     *
     * @author Gabriele
     * @throws RemoteException
     * @throws InterruptedException
     */
    private void penaltyCrew() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.PENALTY_CREW)) {

            Player player = penaltyPlayer;

            // Gets penalty player sender reference
            Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

            // Calculates max crew number available to discard
            int maxCrewCount = player.getSpaceship().getTotalCrewCount();

            if (maxCrewCount > couples.getFirst().getPenalty().getNeededAmount()) {
                sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
                phase = EventPhase.DISCARDED_CREW;

                gameManager.getGameThread().resetAndWaitPlayerReady(penaltyPlayer);

            }

            // Next Couple
            couples.removeFirst();
            phase = EventPhase.CONDITION;
            condition();
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
     * @throws InterruptedException
     */
    @Override
    public void receiveDiscardedCrew(Player player, int xHousingUnit, int yHousingUnit, Sender sender) throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.DISCARDED_CREW)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(penaltyPlayer)) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();
                Component housingUnit = spaceshipMatrix[yHousingUnit][xHousingUnit];

                if (housingUnit != null && housingUnit.getType().equals(ComponentType.HOUSING_UNIT)) {

                    // Checks if a crew member has been discarded
                    if (battlezone.chooseDiscardedCrew(player.getSpaceship(), (HousingUnit) housingUnit)) {
                        requestedCrew--;
                        sender.sendMessage("CrewMemberDiscarded");

                        if (requestedCrew == 0) {

                            player.setIsReady(true, gameManager.getGame());
                            gameManager.getGameThread().notifyThread();

                        } else {
                            sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
                        }

                    } else {
                        sender.sendMessage("NotEnoughCrew");
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
     * Handles box penalty
     *
     * @author Gabriele
     * @throws RemoteException
     * @throws InterruptedException
     */
    private void penaltyBoxes() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.PENALTY_BOXES)) {

            Player player = penaltyPlayer;

            // Gets penalty player sender reference
            Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

            // Box currently owned
            int boxCount = player.getSpaceship().getBoxCounts()[0] + player.getSpaceship().getBoxCounts()[1] + player.getSpaceship().getBoxCounts()[2] + player.getSpaceship().getBoxCounts()[3];

            // Checks if he has at least a box to discard
            if (boxCount > 0) {
                sender.sendMessage(new BoxToDiscardMessage(requestedBoxes));
                phase = EventPhase.DISCARDED_BOXES;

                gameManager.getGameThread().resetAndWaitPlayerReady(penaltyPlayer);

            } else {

                // Checks if he has at least a battery to discard
                if (player.getSpaceship().getBatteriesCount() > 0) {
                    sender.sendMessage("NotEnoughBoxes");
                    sender.sendMessage(new BatteriesToDiscardMessage(requestedBoxes));
                    phase = EventPhase.DISCARDED_BATTERIES_FOR_BOXES;

                    gameManager.getGameThread().resetAndWaitPlayerReady(penaltyPlayer);

                } else {
                    sender.sendMessage("NotEnoughBatteries");
                }
            }

            // Next Couple
            couples.removeFirst();
            phase = EventPhase.CONDITION;
            condition();
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
     * @throws RemoteException
     */
    @Override
    public void receiveDiscardedBox(Player player, int xBoxStorage, int yBoxStorage, int idx, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.DISCARDED_BOXES)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(penaltyPlayer)) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();
                Component boxStorage = spaceshipMatrix[yBoxStorage][xBoxStorage];

                if (boxStorage != null && (boxStorage.getType().equals(ComponentType.BOX_STORAGE) || boxStorage.getType().equals(ComponentType.RED_BOX_STORAGE))) {

                    // Checks if a box has been discarded
                    if (battlezone.chooseDiscardedBox(player.getSpaceship(), (BoxStorage) boxStorage, idx)) {
                        requestedBoxes--;
                        sender.sendMessage("BoxDiscarded");

                        if (requestedBoxes == 0) {

                            player.setIsReady(true, gameManager.getGame());
                            gameManager.getGameThread().notifyThread();

                        } else {

                            // Box currently owned
                            int boxCount = player.getSpaceship().getBoxCounts()[0] + player.getSpaceship().getBoxCounts()[1] + player.getSpaceship().getBoxCounts()[2] + player.getSpaceship().getBoxCounts()[3];

                            if (boxCount > 0) {
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
     * Handles penalty shots
     *
     * @author Gabriele
     * @throws RemoteException
     * @throws InterruptedException
     */
    private void penaltyShot() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.PENALTY_SHOTS)) {

            // Checks if penalty shots are empty
            for (Projectile shot : penaltyShots) {

                // Gets penalty player sender reference
                Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

                sender.sendMessage(new IncomingProjectileMessage(penaltyShots.getFirst().getSize(), penaltyShots.getFirst().getFrom()));

                phase = EventPhase.ASK_ROLL_DICE;
                askToRollDice();

                gameManager.getGameThread().resetAndWaitPlayerReady(penaltyPlayer);
            }

            // Next Couple
            couples.removeFirst();
            phase = EventPhase.CONDITION;
            condition();
        }
    }

    /**
     * Asks first penalized player to roll dice
     *
     * @author Stefano
     * @throws RemoteException
     * @throws InterruptedException
     */
    private void askToRollDice() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.ASK_ROLL_DICE)) {

            // Asks penalty player to roll dice
            Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

            if (penaltyShots.getFirst().getFrom() == 0 || penaltyShots.getFirst().getFrom() == 2) {
                sender.sendMessage("ThrowDiceToFindColumn");

            } else if (penaltyShots.getFirst().getFrom() == 1 || penaltyShots.getFirst().getFrom() == 3) {
                sender.sendMessage("ThrowDiceToFindRow");
            }

            phase = EventPhase.ROLL_DICE;
        }
    }

    /**
     * Rolls dice and collects the result
     *
     * @author Stefano
     * @param player current player
     * @param sender current sender
     * @throws RemoteException
     */
    @Override
    public void rollDice(Player player, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.ROLL_DICE)) {

            // Checks if the player that calls the methods is also the first defeated player
            if (player.equals(penaltyPlayer)) {

                diceResult = player.rollDice();

                sender.sendMessage(new DiceResultMessage(diceResult));
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiceResultMessage(penaltyPlayer.getName(), diceResult), sender);

                if (penaltyShots.getFirst().getSize().equals(ProjectileSize.SMALL)) {
                    phase = EventPhase.ASK_SHIELDS;
                    askToUseShields();

                } else {
                    phase = EventPhase.HANDLE_SHOT;
                    handleShot();
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Asks penalty players if they want to use shields to protect
     *
     * @author Stefano
     * @throws RemoteException
     */
    private void askToUseShields() throws RemoteException {
        if (phase.equals(EventPhase.ASK_SHIELDS)) {

            // Checks if penalty player has a shield that covers that direction
            boolean hasShield = battlezone.checkShields(penaltyPlayer, penaltyShots.getFirst());

            // Asks penalty player if he wants to use a shield
            Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

            if (hasShield && penaltyPlayer.getSpaceship().getBatteriesCount() > 0) {
                sender.sendMessage("AskToUseShield");
                phase = EventPhase.SHIELD_DECISION;

            } else {
                sender.sendMessage("NoShieldAvailable");
                phase = EventPhase.HANDLE_SHOT;
                handleShot();
            }
        }
    }

    /**
     * Receives shield decision
     *
     * @author Gabriele
     * @param player current player
     * @param response player's response
     * @param sender current sender
     * @throws RemoteException
     */
    @Override
    public void receiveProtectionDecision(Player player, String response, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.SHIELD_DECISION)) {

            if (player.equals(penaltyPlayer)) {
                String upperCaseResponse = response.toUpperCase();

                switch (upperCaseResponse) {
                    case "YES":
                        phase = EventPhase.SHIELD_BATTERY;
                        sender.sendMessage(new BatteriesToDiscardMessage(1));
                        break;

                    case "NO":
                        phase = EventPhase.HANDLE_SHOT;
                        handleShot();
                        break;

                    default:
                        sender.sendMessage("IncorrectResponse");
                        break;
                }
            }
        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Handles current shot
     *
     * @author Stefano
     * @throws RemoteException
     */
    private void handleShot() throws RemoteException {
        if (phase.equals(EventPhase.HANDLE_SHOT)) {

            Game game = gameManager.getGame();
            Projectile shot = penaltyShots.getFirst();

            Component destroyedComponent = battlezone.penaltyShot(game, penaltyPlayer, shot, diceResult);

            // Gets penalty player sender reference
            Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

            // Sends two types of messages based on the shot's result
            if (destroyedComponent != null) {
                // TODO: handle waiting in case of needed decision by player on which part of the ship to hold
                SpaceshipController.destroyComponentAndCheckValidity(gameManager, penaltyPlayer, destroyedComponent.getX(), destroyedComponent.getY(), sender);

            } else {
                gameManager.broadcastGameMessage("NothingGotDestroyed");

                penaltyPlayer.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();
            }
        }
    }
}