package org.progetto.server.controller.events;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.controller.LobbyController;
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

    private GameManager gameManager;
    private Slavers slavers;
    private String phase;
    private int currPlayer;
    private ArrayList<Player> activePlayers;
    private float playerFirePower;
    private int requestedBatteries;
    private int requestedCrew;

    // =======================
    // CONSTRUCTORS
    // =======================

    public SlaversController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.slavers = (Slavers) gameManager.getGame().getActiveEventCard();
        this.phase = "START";
        this.currPlayer = 0;
        this.activePlayers = gameManager.getGame().getBoard().getActivePlayers();;
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

            // Retrieves sender reference
            SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(player);
            VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(player);

            Sender sender = null;

            if (socketWriter != null) {
                sender = socketWriter;
            } else if (virtualClient != null) {
                sender = virtualClient;
            }

            // Checks if players is able to win without double cannons
            if (slavers.battleResult(player, player.getSpaceship().getNormalShootingPower()) == 1) {
                phase = "REWARD_DECISION";
                sender.sendMessage(new AcceptRewardCreditsAndPenaltyDays(slavers.getRewardCredits(), slavers.getPenaltyDays()));
            }

            // Calculates max number of double cannons usable
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
                battleResult(player, sender);

            } else {
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
                    battleResult(player, sender);

                } else if (num <= player.getSpaceship().getDoubleEngineCount() && num <= player.getSpaceship().getBatteriesCount() && num > 0) {
                    requestedBatteries = num;

                    Spaceship spaceship = player.getSpaceship();
                    playerFirePower = spaceship.getNormalShootingPower() + 2 * spaceship.getFullDoubleCannonCount() + spaceship.getHalfDoubleCannonCount();
                    // TODO: add this logic to spaceship

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
                    if (slavers.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                        requestedBatteries--;
                        sender.sendMessage("BatteryDiscarded");

                        if (requestedBatteries == 0) {
                            phase = "BATTLE_RESULT";
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
    private void battleResult(Player player, Sender sender) throws RemoteException {
        if (phase.equals("BATTLE_RESULT")) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                // Calls the battleResult function
                switch (slavers.battleResult(player, playerFirePower)){
                    case 1:
                        phase = "REWARD_DECISION";
                        sender.sendMessage(new AcceptRewardCreditsAndPenaltyDays(slavers.getRewardCredits(), slavers.getPenaltyDays()));
                        break;

                    case -1:
                        phase = "PENALTY_EFFECT";
                        penaltyEffect(player, sender);
                        break;

                    case 0:
                        // Next player
                        if (currPlayer < activePlayers.size()) {
                            currPlayer++;
                            phase = "ASK_CANNONS";
                            askHowManyCannonsToUse();
                        } else {
                            phase = "END";
                            end();
                        }
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
        if (phase.equals("PENALTY_EFFECT")) {

            if (player.equals(activePlayers.get(currPlayer))) {

                requestedCrew = slavers.getPenaltyCrew();

                // Calculates max crew number available to discard
                int orangeAlienCount = player.getSpaceship().getAlienOrange() ? 1 : 0;
                int purpleAlienCount = player.getSpaceship().getAlienPurple() ? 1 : 0;
                int crewCount = player.getSpaceship().getCrewCount();
                int maxCrewCount = orangeAlienCount + purpleAlienCount + crewCount;

                if (maxCrewCount > slavers.getPenaltyCrew()) {
                    sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
                    phase = "DISCARDED_CREW";

                } else {
                    sender.sendMessage("NotEnoughCrew");
                    LobbyController.broadcastLobbyMessage(new PlayerDefeatedMessage(player.getName()));
                    gameManager.getGame().getBoard().leaveTravel(player);

                    // Next player
                    if (currPlayer < activePlayers.size()) {
                        currPlayer++;
                        phase = "ASK_CANNONS";
                        askHowManyCannonsToUse();

                    } else {
                        phase = "END";
                        end();
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
        if (phase.equals("DISCARDED_CREW")) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                Component housingUnit = spaceshipMatrix[yHousingUnit][xHousingUnit];

                if (housingUnit != null && housingUnit.getType().equals(ComponentType.HOUSING_UNIT)) {

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
                            } else {
                                phase = "END";
                                end();
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
    public void receiveRewardDecision(Player player, String response, Sender sender) throws RemoteException {
        if (phase.equals("REWARD_DECISION")) {

            if (player.equals(activePlayers.get(currPlayer))) {

                String upperCaseResponse = response.toUpperCase();

                switch (upperCaseResponse) {
                    case "YES":
                        phase = "EFFECT";
                        eventEffect();
                        break;

                    case "NO":
                        phase = "END";
                        end();
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
        if (phase.equals("EFFECT")) {
            Player player = activePlayers.get(currPlayer);
            Board board = gameManager.getGame().getBoard();

            // Event effect applied for single player
            slavers.rewardPenalty(gameManager.getGame().getBoard(), player);

            // Retrieves sender reference
            SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(player);
            VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(player);

            Sender sender = null;

            if (socketWriter != null) {
                sender = socketWriter;
            } else if (virtualClient != null) {
                sender = virtualClient;
            }

            sender.sendMessage(new PlayerMovedBackwardMessage(slavers.getPenaltyDays()));
            sender.sendMessage(new PlayerGetsCreditsMessage(slavers.getRewardCredits()));
            LobbyController.broadcastLobbyMessage(new AnotherPlayerMovedBackwardMessage(player.getName(), slavers.getPenaltyDays()));
            LobbyController.broadcastLobbyMessage(new AnotherPlayerGetsCreditsMessage(player.getName(), slavers.getRewardCredits()));

            // Updates turn order
            board.updateTurnOrder();

            // Checks for lapped player
            ArrayList<Player> lappedPlayers = board.checkLappedPlayers();

            if (lappedPlayers != null) {
                for (Player lappedPlayer : lappedPlayers) {

                    // Gets lapped player sender reference
                    SocketWriter socketWriterLapped = gameManager.getSocketWriterByPlayer(lappedPlayer);
                    VirtualClient virtualClientLapped = gameManager.getVirtualClientByPlayer(lappedPlayer);

                    Sender senderLapped = null;

                    if (socketWriterLapped != null) {
                        senderLapped = socketWriterLapped;
                    } else if (virtualClientLapped != null) {
                        senderLapped = virtualClientLapped;
                    }

                    senderLapped.sendMessage("YouGotLapped");
                    LobbyController.broadcastLobbyMessageToOthers(new PlayerDefeatedMessage(lappedPlayer.getName()), senderLapped);
                    board.leaveTravel(lappedPlayer);
                }
            }

            phase = "END";
            end();
        }
    }

    /**
     * Send a message of end card to all players
     *
     * @author Stefano
     * @throws RemoteException
     */
    private void end() throws RemoteException {
        if (phase.equals("END")) {
            LobbyController.broadcastLobbyMessage("This event card is finished");
        }
    }
}