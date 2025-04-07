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
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.Smugglers;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class SmugglersController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private GameManager gameManager;
    private Smugglers smugglers;
    private String phase;
    private int currPlayer;
    private ArrayList<Player> activePlayers;
    private float playerFirePower;
    private int requestedBatteries;
    private int requestedBoxes;

    // =======================
    // CONSTRUCTORS
    // =======================

    public SmugglersController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.smugglers = (Smugglers) gameManager.getGame().getActiveEventCard();
        this.phase = "START";
        this.currPlayer = 0;
        this.activePlayers = gameManager.getGame().getBoard().getActivePlayers();;
        this.playerFirePower = 0;
        this.requestedBatteries = 0;
        this.requestedBoxes = 0;
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
            Spaceship spaceship = player.getSpaceship();

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            // Checks if players is able to win without double cannons
            if (smugglers.battleResult(player, spaceship.getNormalShootingPower()) == 1) {
                phase = "REWARD_DECISION";
                sender.sendMessage(new AcceptRewardBoxesAndPenaltyDaysMessage(smugglers.getRewardBoxes(), smugglers.getPenaltyDays()));
            }

            // Calculates max number of double cannons usable
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

            // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
            if (maxUsable == 0) {
                playerFirePower = spaceship.getNormalShootingPower();

                phase = "BATTLE_RESULT";
                battleResult(player, sender);

            } else {
                sender.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, smugglers.getFirePowerRequired()));

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

                Spaceship spaceship = player.getSpaceship();

                // Player doesn't want to use double cannons
                if (num == 0) {
                    playerFirePower = player.getSpaceship().getNormalShootingPower();

                    phase = "BATTLE_RESULT";
                    battleResult(player, sender);

                } else if (num <= (spaceship.getFullDoubleCannonCount() + spaceship.getHalfDoubleCannonCount()) && num <= spaceship.getBatteriesCount() && num > 0) {
                    requestedBatteries = num;

                    // Updates player's firepower based on his decision
                    if (num <= spaceship.getFullDoubleCannonCount()) {
                        playerFirePower = spaceship.getNormalShootingPower() + 2 * num;
                    } else {
                        playerFirePower = spaceship.getFullDoubleCannonCount() + 2 * spaceship.getFullDoubleCannonCount() + (num - spaceship.getFullDoubleCannonCount());
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
                    if (smugglers.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
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
                switch (smugglers.battleResult(player, playerFirePower)){
                    case 1:
                        phase = "REWARD_DECISION";
                        sender.sendMessage(new AcceptRewardBoxesAndPenaltyDaysMessage(smugglers.getRewardBoxes(), smugglers.getPenaltyDays()));
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

                requestedBoxes = smugglers.getPenaltyBoxes();

                int maxBoxCount = player.getSpaceship().getBoxCounts()[0]
                        + player.getSpaceship().getBoxCounts()[1]
                        + player.getSpaceship().getBoxCounts()[2]
                        + player.getSpaceship().getBoxCounts()[3];

                if (maxBoxCount >= 1) {
                    sender.sendMessage(new BoxToDiscardMessage(requestedBoxes));
                    phase = "DISCARDED_BOXES";

                } else {
                    sender.sendMessage("NotEnoughBoxes");
                    sender.sendMessage(new BatteriesToDiscardMessage(requestedBoxes));
                    phase = "DISCARDED_BATTERIES_2";
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
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
        if (phase.equals("DISCARDED_BOXES")) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                Component boxStorage = spaceshipMatrix[yBoxStorage][xBoxStorage];

                if (boxStorage != null && boxStorage.getType().equals(ComponentType.BOX_STORAGE)) {

                    // Checks if a crew member has been discarded
                    if (smugglers.chooseDiscardedBox(player.getSpaceship(), (BoxStorage) boxStorage, idx)) {
                        requestedBoxes--;
                        sender.sendMessage("BoxDiscarded");

                        if (requestedBoxes == 0) {

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

                        } else {
                            sender.sendMessage(new BoxToDiscardMessage(requestedBoxes));
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
    public void receiveDiscardedBattery2(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException {
        if (phase.equals("DISCARDED_BATTERIES_2")) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

                if (batteryStorage != null && batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {

                    // Checks if a battery has been discarded
                    if (smugglers.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                        requestedBoxes--;
                        sender.sendMessage("BatteryDiscarded");

                        if (requestedBoxes == 0) {
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

                        } else {
                            sender.sendMessage(new BatteriesToDiscardMessage(requestedBoxes));
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
                        phase = "REWARD_BOXES";
                        //rewardBoxes();
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

    public void rewardBoxes(Player player, Sender sender) throws RemoteException {
        if (phase.equals("REWARD_BOXES")) {

            if (player.equals(activePlayers.get(currPlayer))) {

                // TODO: create this phase for rewardBoxes

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
    private void penaltyDays() throws RemoteException {
        if (phase.equals("PENALTY_DAYS")) {
            Player player = activePlayers.get(currPlayer);
            Board board = gameManager.getGame().getBoard();

            // Event effect applied for single player
            smugglers.penalty(gameManager.getGame().getBoard(), player);

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            sender.sendMessage(new PlayerMovedBackwardMessage(smugglers.getPenaltyDays()));
            LobbyController.broadcastLobbyMessage(new AnotherPlayerMovedBackwardMessage(player.getName(), smugglers.getPenaltyDays()));

            // Updates turn order
            board.updateTurnOrder();

            // Checks for lapped player
            ArrayList<Player> lappedPlayers = board.checkLappedPlayers();

            if (lappedPlayers != null) {
                for (Player lappedPlayer : lappedPlayers) {

                    // Gets lapped player sender reference
                    Sender senderLapped = gameManager.getSenderByPlayer(lappedPlayer);

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