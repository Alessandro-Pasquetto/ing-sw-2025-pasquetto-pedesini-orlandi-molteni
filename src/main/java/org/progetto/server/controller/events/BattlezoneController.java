package org.progetto.server.controller.events;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.AcceptRewardCreditsAndPenaltyDaysMessage;
import org.progetto.messages.toClient.BatteriesToDiscardMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.messages.toClient.HowManyDoubleCannonsMessage;
import org.progetto.messages.toClient.HowManyDoubleEnginesMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.controller.LobbyController;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.events.Battlezone;
import org.progetto.server.model.events.ConditionType;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BattlezoneController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private GameManager gameManager;
    private Battlezone battlezone;
    private String phase;
    private int currPlayer;
    private ArrayList<Player> activePlayers;
    private Map<Player, Integer> tempEnginePower;
    private Map<Player, Float> tempFirePower;
    private Player penaltyPlayer;
    private int requestedBatteries;
    private int requestedCrew;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BattlezoneController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.battlezone = (Battlezone) gameManager.getGame().getActiveEventCard();
        this.phase = "START";
        this.currPlayer = 0;
        this.activePlayers = gameManager.getGame().getBoard().getActivePlayers();
        this.tempEnginePower = new HashMap<>();
        this.tempFirePower = new HashMap<>();
        this.penaltyPlayer = null;
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
    public void start() throws RemoteException {

        switch (battlezone.getCouples().getFirst().getType()){

            case ConditionType.CREWREQUIREMENT:
                phase = "CREW_COUNT";
                break;

            case ConditionType.FIREPOWERREQUIREMENT:
                phase = "ASK_ENGINES";
                askHowManyEnginesToUse();
                break;

            case ConditionType.ENGINEPOWERREQUIREMENT:
                phase = "ASK_CANNONS";
                askHowManyCannonsToUse();
                break;
        }
    }

    /**
     * Finds player with fewer crew
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void choseFewerCrew() throws RemoteException {
        if (phase.equals("CREW_COUNT")) {

            // Finds player with fewer crew
            penaltyPlayer = battlezone.lessPopulatedSpaceship(activePlayers);

            phase = "PENALTY";
        }
    }

    /**
     * Asks current player how many double engines he wants to use
     *
     * @author Stefano
     */
    private void askHowManyEnginesToUse() throws RemoteException {
        if (phase.equals("ASK_ENGINES")) {

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
                    phase = "ASK_ENGINES";
                    askHowManyEnginesToUse();

                } else {
                    sender.sendMessage(new HowManyDoubleEnginesMessage(maxUsable));
                    phase = "ENGINE_NUMBER";
                }

            } else {
                phase = "CHOOSE_WEAKEST_ENGINES";
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
        if (phase.equals("ENGINE_NUMBER")) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                // Player doesn't want to use double engines
                if (num == 0) {
                    tempEnginePower.put(player, player.getSpaceship().getNormalEnginePower());

                    // Next player
                    currPlayer++;
                    phase = "ASK_ENGINES";
                    askHowManyEnginesToUse();

                } else if (num <= player.getSpaceship().getDoubleEngineCount() && num <= player.getSpaceship().getBatteriesCount() && num > 0) {
                    requestedBatteries = num;
                    tempEnginePower.put(player, player.getSpaceship().getNormalEnginePower() + 2 * num);

                    sender.sendMessage(new BatteriesToDiscardMessage(num));

                    phase = "DISCARDED_BATTERIES";

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
        if (phase.equals("ASK_CANNONS")) {

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
                    phase = "ASK_CANNONS";
                    askHowManyCannonsToUse();

                } else {
                    sender.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, 0));
                    phase = "CANNON_NUMBER";
                }

            } else {
                phase = "CHOOSE_WEAKEST_CANNONS";
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
        if (phase.equals("CANNON_NUMBER")) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                Spaceship spaceship = player.getSpaceship();

                // Player doesn't want to use double cannons
                if (num == 0) {
                    tempFirePower.put(player, player.getSpaceship().getNormalShootingPower());

                    // Next player
                    currPlayer++;
                    phase = "ASK_CANNONS";
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
                    phase = "DISCARDED_BATTERIES";

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
        if (phase.equals("DISCARDED_BATTERIES")) {

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

                            if (battlezone.getCouples().getFirst().getType() == ConditionType.ENGINEPOWERREQUIREMENT){
                                phase = "ASK_ENGINES";
                                askHowManyEnginesToUse();

                            } else if (battlezone.getCouples().getFirst().getType() == ConditionType.FIREPOWERREQUIREMENT) {
                                phase = "ASK_CANNONS";
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
    private void chooseWeakestEngines() {
        if (phase.equals("CHOOSE_WEAKEST_ENGINES")) {
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

            phase = "PENALTY";
            penalty();
        }
    }

    /**
     * Finds player with less firepower
     *
     * @author Gabriele
     */
    private void chooseWeakestCannons() {
        if (phase.equals("CHOOSE_WEAKEST_CANNONS")) {
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

            phase = "PENALTY";
            penalty();
        }
    }

    public void penalty() {
        if (phase.equals("CANNON_NUMBER")) {

        }

        // TODO: remember to reset controller temp values like currPlayer, tempFirePower, tempEnginePower...
    }
}