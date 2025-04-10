package org.progetto.server.controller.events;

import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventCommon.IncomingProjectileMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
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
        this.couples = new ArrayList<>(couples);
        this.penaltyShots = new ArrayList<>();
        this.activePlayers = gameManager.getGame().getBoard().getCopyActivePlayers();
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
     */
    @Override
    public void start() throws RemoteException {
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
     */
    private void condition() throws RemoteException {
        if (phase.equals(EventPhase.CONDITION)) {

            // Checks if all the couples were evaluated
            if (!couples.isEmpty()) {

                switch (couples.getFirst().getCondition()){

                    case ConditionType.CREWREQUIREMENT:
                        phase = EventPhase.CREW_COUNT;
                        choseFewerCrew();
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

            } else {
                phase = EventPhase.END;
                end();
            }
        }
    }

    /**
     * Finds player with fewer crew
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void choseFewerCrew() throws RemoteException {
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
     */
    private void askHowManyEnginesToUse() throws RemoteException {
        if (phase.equals(EventPhase.ASK_ENGINES)) {

            // Checks if every player took a decision about how many engines to use
            if (currPlayer < activePlayers.size()) {

                Player player = activePlayers.get(currPlayer);

                // Gets the sender reference to send a message to player
                Sender sender = gameManager.getSenderByPlayer(player);

                // Calculates max number of double engine usable
                int maxUsable = player.getSpaceship().maxNumberOfDoubleEnginesUsable();

                // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
                if (maxUsable == 0) {
                    tempEnginePower.put(player, player.getSpaceship().getNormalEnginePower());

                    // Next player
                    currPlayer++;
                    phase = EventPhase.ASK_ENGINES;
                    askHowManyEnginesToUse();

                } else {
                    sender.sendMessage(new HowManyDoubleEnginesMessage(maxUsable));
                    phase = EventPhase.ENGINE_NUMBER;
                }

            } else {
                phase = EventPhase.CHOOSE_WEAKEST_ENGINES;
                chooseWeakestEngines();
            }
        }
    }

    /**
     * Receives numbers of double engines to use
     *
     * @author Stefano
     */
    public void receiveHowManyEnginesToUse(Player player, int num, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.ENGINE_NUMBER)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                // Player doesn't want to use double engines
                if (num == 0) {
                    tempEnginePower.put(player, player.getSpaceship().getNormalEnginePower());

                    // Next player
                    currPlayer++;
                    phase = EventPhase.ASK_ENGINES;
                    askHowManyEnginesToUse();

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
     */
    private void askHowManyCannonsToUse() throws RemoteException {
        if (phase.equals(EventPhase.ASK_CANNONS)) {

            // Checks if every player took a decision about how many engines to use
            if (currPlayer < activePlayers.size()) {
                Player player = activePlayers.get(currPlayer);
                Spaceship spaceship = player.getSpaceship();

                // Retrieves sender reference
                Sender sender = gameManager.getSenderByPlayer(player);

                // Calculates max number of double cannons usable
                int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

                // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
                if (maxUsable == 0) {
                    tempFirePower.put(player, player.getSpaceship().getNormalShootingPower());

                    // Next player
                    currPlayer++;
                    phase = EventPhase.ASK_CANNONS;
                    askHowManyCannonsToUse();

                } else {
                    sender.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, 0));
                    phase = EventPhase.CANNON_NUMBER;
                }

            } else {
                phase = EventPhase.CHOOSE_WEAKEST_CANNONS;
                chooseWeakestCannons();
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
    public void receiveHowManyCannonsToUse(Player player, int num, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.CANNON_NUMBER)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                Spaceship spaceship = player.getSpaceship();

                // Player doesn't want to use double cannons
                if (num == 0) {
                    tempFirePower.put(player, player.getSpaceship().getNormalShootingPower());

                    // Next player
                    currPlayer++;
                    phase = EventPhase.ASK_CANNONS;
                    askHowManyCannonsToUse();

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
     * @param player
     * @param xBatteryStorage
     * @param yBatteryStorage
     * @param sender
     * @throws RemoteException
     */
    public void receiveDiscardedBattery(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

                if (batteryStorage != null && batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {

                    // Checks if a battery has been discarded
                    if (battlezone.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                        requestedBatteries--;
                        sender.sendMessage("BatteryDiscarded");

                        if (requestedBatteries == 0) {

                            // Next player
                            currPlayer++;

                            if (couples.getFirst().getCondition() == ConditionType.ENGINEPOWERREQUIREMENT) {
                                phase = EventPhase.ASK_ENGINES;
                                askHowManyEnginesToUse();

                            } else if (couples.getFirst().getCondition() == ConditionType.FIREPOWERREQUIREMENT) {
                                phase = EventPhase.ASK_CANNONS;
                                askHowManyCannonsToUse();
                            }

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
     * Finds player with less engine power
     *
     * @author Gabriele
     */
    private void chooseWeakestEngines() throws RemoteException {
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
     */
    private void chooseWeakestCannons() throws RemoteException {
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
     */
    public void penalty() throws RemoteException {
        if (phase.equals(EventPhase.PENALTY)) {

            // Gets penalty player sender reference
            Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

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
        currPlayer = 0;
        tempFirePower.clear();
        tempEnginePower.clear();
    }

    /**
     * Player loses penaltyDays
     *
     * @author Stefano
     * @throws RemoteException
     */
    private void penaltyDays() throws RemoteException {
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

            // Checks for lapped player
            ArrayList<Player> lappedPlayers = board.checkLappedPlayers();

            if (lappedPlayers != null) {
                for (Player lappedPlayer : lappedPlayers) {

                    // Gets lapped player sender reference
                    Sender senderLapped = gameManager.getSenderByPlayer(penaltyPlayer);

                    senderLapped.sendMessage("YouGotLapped");
                    gameManager.broadcastGameMessageToOthers(new PlayerDefeatedMessage(lappedPlayer.getName()), senderLapped);
                    board.leaveTravel(lappedPlayer);
                }
            }

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
     */
    private void penaltyCrew() throws RemoteException {
        if (phase.equals(EventPhase.PENALTY_CREW)) {

            Player player = penaltyPlayer;

            // Gets penalty player sender reference
            Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

            // Calculates max crew number available to discard
            int maxCrewCount = player.getSpaceship().getTotalCrewCount();

            if (maxCrewCount > couples.getFirst().getPenalty().getNeededAmount()) {
                sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
                phase = EventPhase.DISCARDED_CREW;

            } else {
                // Player is defeated
                sender.sendMessage("NotEnoughCrew");
                gameManager.broadcastGameMessage(new PlayerDefeatedMessage(player.getName()));
                gameManager.getGame().getBoard().leaveTravel(player);

                // Next Couple
                couples.removeFirst();
                phase = EventPhase.CONDITION;
                condition();
            }
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
    public void receiveDiscardedCrew(Player player, int xHousingUnit, int yHousingUnit, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.DISCARDED_CREW)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(penaltyPlayer)) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                Component housingUnit = spaceshipMatrix[yHousingUnit][xHousingUnit];

                if (housingUnit != null && housingUnit.getType().equals(ComponentType.HOUSING_UNIT)) {

                    // Checks if a crew member has been discarded
                    if (battlezone.chooseDiscardedCrew(player.getSpaceship(), (HousingUnit) housingUnit)) {
                        requestedCrew--;
                        sender.sendMessage("CrewMemberDiscarded");

                        if (requestedCrew == 0) {

                            // Next Couple
                            couples.removeFirst();
                            phase = EventPhase.CONDITION;
                            condition();

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
     */
    private void penaltyBoxes() throws RemoteException {
        if (phase.equals(EventPhase.PENALTY_BOXES)) {

            Player player = penaltyPlayer;

            // Gets penalty player sender reference
            Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

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

                    // Next Couple
                    couples.removeFirst();
                    phase = EventPhase.CONDITION;
                    condition();
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
            if (player.equals(penaltyPlayer)) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                Component boxStorage = spaceshipMatrix[yBoxStorage][xBoxStorage];

                if (boxStorage != null && (boxStorage.getType().equals(ComponentType.BOX_STORAGE) || boxStorage.getType().equals(ComponentType.RED_BOX_STORAGE))) {

                    // Checks if a box has been discarded
                    if (battlezone.chooseDiscardedBox(player.getSpaceship(), (BoxStorage) boxStorage, idx)) {
                        requestedBoxes--;
                        sender.sendMessage("BoxDiscarded");

                        if (requestedBoxes == 0) {

                            // Next Couple
                            couples.removeFirst();
                            phase = EventPhase.CONDITION;
                            condition();

                        } else {
                            phase = EventPhase.PENALTY_BOXES;
                            penaltyBoxes();
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
            if (player.equals(penaltyPlayer)) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

                if (batteryStorage != null && batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {

                    // Checks if a battery has been discarded
                    if (battlezone.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                        requestedBoxes--;
                        sender.sendMessage("BatteryDiscarded");

                        if (requestedBoxes == 0) {

                            // Next Couple
                            couples.removeFirst();
                            phase = EventPhase.CONDITION;
                            condition();

                        } else {
                            phase = EventPhase.PENALTY_BOXES;
                            penaltyBoxes();
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

    private void penaltyShot() throws RemoteException {
        if (phase.equals(EventPhase.PENALTY_SHOTS)) {

            // Checks if penalty shots are empty
            if (!penaltyShots.isEmpty()) {
                // Gets penalty player sender reference
                Sender sender = gameManager.getSenderByPlayer(penaltyPlayer);

                sender.sendMessage(new IncomingProjectileMessage(penaltyShots.getFirst().getSize(), penaltyShots.getFirst().getFrom()));

                phase = EventPhase.ASK_ROLL_DICE;
                askToRollDice();

            } else {
                // Next Couple
                couples.removeFirst();
                phase = EventPhase.CONDITION;
                condition();
            }
        }
    }

    /**
     * Asks first penalized player to roll dice
     *
     * @author Stefano
     * @throws RemoteException
     */
    private void askToRollDice() throws RemoteException {
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
     * @param player
     * @param sender
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
     *
     * @Stefano
     * @param player
     * @param response
     * @param sender
     * @throws RemoteException
     */
    public void receiveShieldDecision(Player player, String response, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.SHIELD_DECISION)) {

            if (player.equals(penaltyPlayer)) {
                String upperCaseResponse = response.toUpperCase();

                switch (upperCaseResponse) {
                    case "YES":
                        phase = EventPhase.SHIELD_BATTERY;
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
     * Receives the coordinates of BatteryStorage component from which remove a battery to activate shield
     *
     * @author Stefano
     * @param player
     * @param xBatteryStorage
     * @param yBatteryStorage
     * @param sender
     * @throws RemoteException
     */
    public void receiveShieldBattery(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.SHIELD_BATTERY)) {

            // Checks if the player that calls the methods has to discard a battery to activate a shield
            if (player.equals(penaltyPlayer)) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
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
            }

            // Checks if penalty player lost
            int totalCrew = penaltyPlayer.getSpaceship().getTotalCrewCount();

            if (totalCrew == 0) {
                gameManager.broadcastGameMessage(new PlayerDefeatedMessage(penaltyPlayer.getName()));
                gameManager.getGame().getBoard().leaveTravel(penaltyPlayer);
            }

            // Removes just handled shot
            penaltyShots.removeFirst();

            // Next shot
            phase = EventPhase.PENALTY_SHOTS;
            penaltyShot();
        }
    }

    /**
     * Send a message of end card to all players
     *
     * @author Stefano
     * @throws RemoteException
     */
    private void end() throws RemoteException {
        if (phase.equals(EventPhase.END)) {
            gameManager.broadcastGameMessage("This event card is finished");
        }
    }
}