package org.progetto.server.controller.events;

import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.messages.toClient.LostShip.AnotherPlayerLandedMessage;
import org.progetto.messages.toClient.EventCommon.AvailableBoxesMessage;
import org.progetto.messages.toClient.PlayerMovedBackwardMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
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
    
    private LostStation lostStation;
    private ArrayList<Player> activePlayers;
    private ArrayList<Box> rewardBoxes;
    
    // =======================
    // CONSTRUCTORS
    // =======================

    public LostStationController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.phase = EventPhase.START;
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();
        this.lostStation = (LostStation) gameManager.getGame().getActiveEventCard();
        this.rewardBoxes = new ArrayList<>();
    }

    // =======================
    // OTHER METHODS
    // =======================

    @Override
    public void start() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.START)) {
            phase = EventPhase.ASK_TO_LAND;
            askForLand();
        }
    }

    /**
     * Ask each player if they want to land on the lost ship, only if the preconditions are satisfied
     *
     * @author Lorenzo
     * @throws RemoteException
     */
    private void askForLand() throws RemoteException, IllegalStateException, InterruptedException {
        if (phase.equals(EventPhase.ASK_TO_LAND)) {

            for (Player player : activePlayers) {

                gameManager.getGame().setActivePlayer(player);

                Sender sender = gameManager.getSenderByPlayer(player);

                if (player.getSpaceship().getCrewCount() >= lostStation.getRequiredCrew()) {
                    sender.sendMessage("LandRequest");
                    phase = EventPhase.LAND;

                    gameManager.getGameThread().waitPlayerReady(player);

                } else {
                    sender.sendMessage("NotEnoughCrew");
                }
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
    public void receiveDecisionToLand(Player player, String decision, Sender sender) throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.LAND)) {

            if (player.equals(gameManager.getGame().getActivePlayer())) {

                String upperCaseDecision = decision.toUpperCase();

                switch(upperCaseDecision) {
                    case "YES":
                        phase = EventPhase.CHOOSE_BOX;
                        rewardBoxes = lostStation.getRewardBoxes();

                        sender.sendMessage("LandingCompleted");
                        gameManager.broadcastGameMessage(new AnotherPlayerLandedMessage(player));

                        sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));

                    case "NO":
                        phase = EventPhase.ASK_TO_LAND;

                        player.setIsReady(true, gameManager.getGame());
                        gameManager.getGameThread().notifyThread();

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
     * @param idxBox chosen
     * @param y coordinate of the component were the box will be placed
     * @param x coordinate of the component were the box will be placed
     * @param idx is where the player want to insert the chosen box
     * @param sender
     * @throws RemoteException
     */
    public void receiveRewardBox(Player player, int idxBox, int y, int x, int idx, Sender sender) throws RemoteException, IllegalStateException {
        if (phase.equals(EventPhase.CHOOSE_BOX)) {

            // Checks that current player is trying to get reward the reward box
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                try {
                    Component[][] matrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();
                    BoxStorage storage = (BoxStorage) matrix[y][x];
                    Box box = rewardBoxes.get(idxBox);

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
        if (phase.equals(EventPhase.CHOOSE_BOX)) {

            // Checks that current player is trying to leave
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                Board board = gameManager.getGame().getBoard();

                lostStation.penalty(gameManager.getGame().getBoard(), player);

                sender.sendMessage(new PlayerMovedBackwardMessage(lostStation.getPenaltyDays()));
                gameManager.broadcastGameMessage(new AnotherPlayerMovedBackwardMessage(player.getName(), lostStation.getPenaltyDays()));

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }
}