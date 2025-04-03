package org.progetto.server.controller.events;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.*;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.controller.EventController;
import org.progetto.server.controller.LobbyController;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;
import org.progetto.server.model.events.Slavers;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class SlaversController {

    // =======================
    // ATTRIBUTES
    // =======================

    private GameManager gameManager;
    private String phase;
    private int currPlayer;
    private ArrayList<Player> activePlayers;
    private float playerFirePower;
    private int requestedNumber;
    private int requestedCrew;


    // =======================
    // CONSTRUCTORS
    // =======================

    public SlaversController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.phase = "START";
        this.currPlayer = 0;
        this.activePlayers = gameManager.getGame().getBoard().getActivePlayers();;
        this.playerFirePower = 0;
        this.requestedNumber = 0;
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
    public void start() throws RemoteException {
        phase = "ASK_CANNONS";
        askHowManyCannonsToUse();
    }

    /**
     * Asks current player how many double cannons he wants to use
     *
     * @author Stefano
     * @throws RemoteException
     */
    private void askHowManyCannonsToUse() throws RemoteException {
        if (phase.equals("ASK_CANNONS")) {

            Player player = activePlayers.get(currPlayer);

            Slavers slavers = (Slavers) gameManager.getGame().getActiveEventCard();
            if (slavers.battleResult(player, player.getSpaceship().getNormalShootingPower()) == 1){
                phase = "EVENT_EFFECT";
                eventEffect();
            }

            int doubleFireCount = player.getSpaceship().getDoubleCannonCount();
            int batteriesCount = player.getSpaceship().getBatteriesCount();
            int maxUsable;

            if (doubleFireCount < batteriesCount) {
                maxUsable = doubleFireCount;
            } else {
                maxUsable = batteriesCount;
            }

            // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
            if (maxUsable == 0) {
                playerFirePower = player.getSpaceship().getNormalShootingPower();

                phase = "BATTLE_RESULT";

            } else {
                SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(player);
                VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(player);

                Sender sender = null;

                if (socketWriter != null) {
                    sender = socketWriter;
                } else if (virtualClient != null) {
                    sender = virtualClient;
                }

                sender.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, slavers.getFirePowerRequired()));

                phase = "CANNON_NUMBER";
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

                // Player doesn't want to use double cannons
                if (num == 0) {
                    playerFirePower = player.getSpaceship().getNormalShootingPower();

                    phase = "BATTLE_RESULT";

                } else if (num <= player.getSpaceship().getDoubleEngineCount() && num <= player.getSpaceship().getBatteriesCount() && num > 0) {
                    requestedNumber = num;
                    playerFirePower = player.getSpaceship().getNormalShootingPower() + 2 * num;

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
                    Slavers slavers = (Slavers) gameManager.getGame().getActiveEventCard();

                    // Checks if a battery has been discarded
                    if (slavers.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                        requestedNumber--;
                        sender.sendMessage("BatteryDiscarded");

                        if (requestedNumber == 0) {
                            phase = "BATTLE_RESULT";

                        } else {
                            sender.sendMessage(new BatteriesToDiscardMessage(requestedNumber));
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
    private void battleResult(Player player, Sender sender) throws RemoteException {
        if (phase.equals("BATTLE_RESULT")) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                // Calls the battleResult function
                Slavers slavers = (Slavers) gameManager.getGame().getActiveEventCard();
                switch (slavers.battleResult(player, playerFirePower)){
                    case 1:
                        sender.sendMessage(new AcceptRewardCreditsAndPenaltyDays(slavers.getRewardCredits(), slavers.getPenaltyDays()));
                        phase = "GET_RESPONSE";

                    case -1:
                        phase = "PENALTY_EFFECT";

                    case 0:
                        // Next player
                        if (currPlayer < activePlayers.size()) {
                            currPlayer++;
                            phase = "ASK_CANNONS";
                            askHowManyCannonsToUse();
                        }
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * if the player is defeated he suffers the penalty
     *
     * @author Stefano
     * @param player
     * @param sender
     * @throws RemoteException
     */
    public void penaltyEffect(Player player, Sender sender) throws RemoteException {
        if (phase.equals("PENALTY_EFFECT")) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                Slavers slavers = (Slavers) gameManager.getGame().getActiveEventCard();
                requestedCrew = slavers.getPenaltyCrew();
                sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
                phase = "DISCARDED_CREW";

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Receives the coordinates of HousingUnit component from which remove a battery
     *
     * @author Stefano
     * @param player
     * @param xHousingUnit
     * @param yHousingUnit
     * @param sender
     * @throws RemoteException
     */
    public void receiveDiscardedCrew(Player player, int xHousingUnit, int yHousingUnit, Sender sender) throws RemoteException {
        if (phase.equals("DISCARDED_CREW")) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                Component housingUnit = spaceshipMatrix[yHousingUnit][xHousingUnit];

                if (housingUnit != null && housingUnit.getType().equals(ComponentType.HOUSING_UNIT)) {
                    Slavers slavers = (Slavers) gameManager.getGame().getActiveEventCard();

                    // Checks if a crew member has been discarded
                    if (slavers.chooseDiscardedCrew(player.getSpaceship(), (HousingUnit) housingUnit)) {
                        requestedCrew--;
                        sender.sendMessage("CrewMemberDiscarded");

                        if (requestedCrew == 0) {
                            // Next player
                            if (currPlayer < activePlayers.size()) {
                                currPlayer++;
                                phase = "ASK_CANNONS";
                                askHowManyCannonsToUse();
                            }

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
    public void getResponse(Player player, String response, Sender sender) throws RemoteException {
        String upperCaseResponse = response.toUpperCase();
        switch (upperCaseResponse) {
            case "YES":
                phase = "EVENT_EFFECT";
                eventEffect();
            case "NO":
                phase = "END";
                endEvent();
            default: sender.sendMessage("IncorrectResponse");
        }
    }

    /**
     * If the player accepted, he receives the reward and loses the penalty days
     *
     * @author Stefano
     * @throws RemoteException
     */
    private void eventEffect() throws RemoteException {
        if (phase.equals("EVENT_EFFECT")) {
            Player player = activePlayers.get(currPlayer);
            Slavers slavers = (Slavers) gameManager.getGame().getActiveEventCard();

            // Event effect applied for single player
            slavers.rewardPenalty(gameManager.getGame().getBoard(), player);

            SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(player);
            VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(player);

            Sender sender = null;

            if (socketWriter != null) {
                sender = socketWriter;
            } else if (virtualClient != null) {
                sender = virtualClient;
            }

            sender.sendMessage(new PlayerMovedBackwardsMessage(slavers.getPenaltyDays()));
            sender.sendMessage(new PlayerGetsCreditsMessage(slavers.getRewardCredits()));
            LobbyController.broadcastLobbyMessage(new AnotherPlayerMovedBackwardsMessage(player.getName(), slavers.getPenaltyDays()));
            LobbyController.broadcastLobbyMessage(new AnotherPlayerGetsCreditsMessage(player.getName(), slavers.getRewardCredits()));

            phase = "END";
        }
    }

    /**
     * Send a message of end card to all players
     *
     * @author Stefano
     * @throws RemoteException
     */
    private void endEvent() throws RemoteException {
        if (phase.equals("END")) {
            LobbyController.broadcastLobbyMessage("This event card is finished");
        }
    }
}