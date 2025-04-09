package org.progetto.server.controller.events;

import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.messages.toClient.LostShip.AnotherPlayerLandedMessage;
import org.progetto.messages.toClient.EventCommon.AvailableBoxesMessage;
import org.progetto.messages.toClient.PlayerMovedBackwardMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.events.LostStation;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class LostStationController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private GameManager gameManager;
    private LostStation lostStation;
    private String phase;
    private int currPlayer;
    private ArrayList<Player> activePlayers;
    boolean boxChosen = false;
    private ArrayList<Box> rewardBoxes;


    // =======================
    // CONSTRUCTORS
    // =======================

    public LostStationController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.phase = "START";
        this.currPlayer = 0;
        this.activePlayers = gameManager.getGame().getBoard().getCopyActivePlayers();
        this.lostStation = (LostStation) gameManager.getGame().getActiveEventCard();
        this.rewardBoxes = new ArrayList<>();
    }

    // =======================
    // GETTERS
    // =======================

    @Override
    public String getPhase() throws RemoteException {
        return phase;
    }

    @Override
    public Player getCurrPlayer() throws RemoteException {
        return activePlayers.get(currPlayer);
    }

    // =======================
    // OTHER METHODS
    // =======================

    @Override
    public void start() throws RemoteException {
        phase = "ASK_FOR_LAND";
        askForLand();
    }

    /**
     * Ask each player if they want to land on the lost ship, only if the preconditions are satisfied
     *
     * @author Lorenzo
     * @throws RemoteException
     */
    private void askForLand() throws RemoteException,IllegalStateException {

        if(phase.equals("ASK_FOR_LAND")) {

            if (currPlayer < activePlayers.size()) {
                Player player = activePlayers.get(currPlayer);

                Sender sender = gameManager.getSenderByPlayer(player);

                if (player.getSpaceship().getCrewCount() >= lostStation.getRequiredCrew()) {
                    sender.sendMessage("LandRequest");
                    phase = "LAND";

                } else {
                    sender.sendMessage("NotEnoughCrew");
                }

            } else {
                phase = "END";
                end();
            }
        }
    }

    /**
     * Receive the player decision to land on the lost ship
     * Send the available boxes to that player
     *
     * @author Gabriele
     * @param player
     * @param sender
     * @param decision
     * @throws RemoteException
     */
    public void receiveDecisionToLand(Player player, String decision, Sender sender) throws RemoteException {
        if (phase.equals("LAND")) {

            if (player.equals(activePlayers.get(currPlayer))) {

                String upperCaseDecision = decision.toUpperCase();

                switch(upperCaseDecision) {
                    case "YES":
                        phase = "CHOOSE_BOX";
                        rewardBoxes = lostStation.getRewardBoxes();

                        gameManager.broadcastGameMessage(new AnotherPlayerLandedMessage(player));
                        sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
                        sender.sendMessage("LandingCompleted");

                    case "NO":
                        phase = "ASK_FOR_LAND";
                        currPlayer++;
                        askForLand();

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
     * Receive the box that the player choose, and it's placement in the component
     * Update the player's view with the new list of available boxes
     *
     * @author Lorenzo
     * @param player that choose the box
     * @param box chosen
     * @param y coordinate of the component were the box will be placed
     * @param x coordinate of the component were the box will be placed
     * @param idx is where the player want to insert the chosen box
     * @param sender
     * @throws RemoteException
     */
    public void receiveRewardBox(Player player, Box box, int y, int x, int idx, Sender sender) throws RemoteException, IllegalStateException {
        if (phase.equals("CHOOSE_BOX")) {

            // Checks that current player is trying to get reward the reward box
            if (player.equals(activePlayers.get(currPlayer))) {

                try {
                    Component[][] matrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                    BoxStorage storage = (BoxStorage) matrix[y][x];

                    // Checks box chosen is contained in rewards list
                    if (rewardBoxes.contains(box)) {

                        // Checks that reward box is placed correctly in given storage
                        if (lostStation.chooseRewardBox(player.getSpaceship(), storage, idx, box)) {
                            sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
                            sender.sendMessage("BoxChosen");

                            rewardBoxes.remove(box);

                        } else {
                            sender.sendMessage("BoxNotChosen");
                        }

                    } else {
                        sender.sendMessage("ChosenBoxNotAvailable");
                    }

                } catch (ClassCastException e) {
                    throw new IllegalStateException("ComponentIsNotAStorage");

                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new IllegalStateException("ComponentIsNotInMatrix");
                }

                // All the boxes are chosen
                if (rewardBoxes.isEmpty()) {
                    leaveStation(player, sender);
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Handles the penalty after player left station
     *
     * @author Lorenzo
     */
    public void leaveStation(Player player, Sender sender) throws RemoteException {
        if (phase.equals("CHOSE_BOX")) {

            // Checks that current player is trying to leave
            if (player.equals(activePlayers.get(currPlayer))) {
                Board board = gameManager.getGame().getBoard();

                lostStation.penalty(gameManager.getGame().getBoard(), player);

                sender.sendMessage(new PlayerMovedBackwardMessage(lostStation.getPenaltyDays()));
                gameManager.broadcastGameMessage(new AnotherPlayerMovedBackwardMessage(player.getName(), lostStation.getPenaltyDays()));

                // Updates turn order
                board.updateTurnOrder();

                // Checks for lapped player
                ArrayList<Player> lappedPlayers = board.checkLappedPlayers();

                if (lappedPlayers != null) {
                    for (Player lappedPlayer : lappedPlayers) {

                        // Gets lapped player sender reference
                        Sender senderLapped = gameManager.getSenderByPlayer(lappedPlayer);

                        senderLapped.sendMessage("YouGotLapped");
                        gameManager.broadcastGameMessageToOthers(new PlayerDefeatedMessage(lappedPlayer.getName()), senderLapped);
                        board.leaveTravel(lappedPlayer);
                    }
                }

                phase = "END";
                end();

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
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
            gameManager.broadcastGameMessage("This event card is finished");
        }
    }
}