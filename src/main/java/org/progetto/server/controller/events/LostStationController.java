package org.progetto.server.controller.events;

import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventCommon.AvailableBoxesMessage;
import org.progetto.messages.toClient.EventCommon.PlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventCommon.AnotherPlayerLandedMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.events.LostStation;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class LostStationController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private final GameManager gameManager;
    private LostStation lostStation;
    private ArrayList<Player> activePlayers;
    private ArrayList<Box> rewardBoxes;
    private boolean someoneLanded;

    // =======================
    // CONSTRUCTORS
    // =======================

    public LostStationController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.phase = EventPhase.START;
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();
        this.lostStation = (LostStation) gameManager.getGame().getActiveEventCard();
        this.rewardBoxes = new ArrayList<>();
        this.someoneLanded = false;
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
     * @throws IllegalStateException
     * @throws InterruptedException
     */
    private void askForLand() throws RemoteException, IllegalStateException, InterruptedException {
        if (phase.equals(EventPhase.ASK_TO_LAND)) {

            for (Player player : activePlayers) {

                gameManager.getGame().setActivePlayer(player);

                Sender sender = gameManager.getSenderByPlayer(player);

                if (player.getSpaceship().getTotalCrewCount() >= lostStation.getRequiredCrew()) {
                    sender.sendMessage("LandRequest");
                    phase = EventPhase.LAND;

                    gameManager.getGameThread().resetAndWaitPlayerReady(player);

                } else {
                    sender.sendMessage("NotEnoughCrew");
                }

                // Checks if someone landed on the station
                if (someoneLanded) return;
            }
        }
    }

    /**
     * Receive the player decision to land on the lost ship
     * Send the available boxes to that player
     *
     * @author Gabriele
     * @param player current player
     * @param sender current sender
     * @param decision player's decision
     * @throws RemoteException
     */
    @Override
    public void receiveDecisionToLand(Player player, String decision, Sender sender) throws RemoteException, InterruptedException {
        if (!phase.equals(EventPhase.LAND)) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            sender.sendMessage("NotYourTurn");
            return;
        }

        String upperCaseDecision = decision.toUpperCase();

        switch (upperCaseDecision) {
            case "YES":
                phase = EventPhase.CHOOSE_BOX;
                someoneLanded = true;
                rewardBoxes = lostStation.getRewardBoxes();

                sender.sendMessage("LandingCompleted");
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerLandedMessage(player), sender);

                sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
                break;

            case "NO":
                phase = EventPhase.ASK_TO_LAND;

                player.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();
                break;

            default:
                sender.sendMessage("IncorrectResponse");
                sender.sendMessage("LandRequest");
                break;
        }
    }

    /**
     * Receive the box that the player choose, and it's placement in the component
     * If player wants to leave he selects idxBox = -1
     *
     * @author Lorenzo
     * @param player that choose the box
     * @param idxBox chosen
     * @param x coordinate of the component were the box will be placed
     * @param y coordinate of the component were the box will be placed
     * @param idx is where the player want to insert the chosen box
     * @param sender current sender
     * @throws RemoteException
     */
    @Override
    public void receiveRewardBox(Player player, int idxBox, int x, int y, int idx, Sender sender) throws RemoteException, IllegalStateException {
        if (!phase.equals(EventPhase.CHOOSE_BOX)) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        // Checks that current player is trying to get reward the reward box
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            sender.sendMessage("NotYourTurn");
            return;
        }

        // Checks if reward box index is correct
        if (idxBox < -1 || idxBox >= rewardBoxes.size()) {
            sender.sendMessage("IncorrectRewardIndex");
            sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
            return;
        }

        // Checks if player wants to leave
        if (idxBox == -1) {
            leaveStation(player, sender);
            return;
        }

        Component[][] matrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();

        // Checks if component index is correct
        if (x < 0 || y < 0 || y >= matrix.length || x >= matrix[0].length ) {
            sender.sendMessage("InvalidCoordinates");
            sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
            return;
        }

        Component component = matrix[y][x];

        // Checks if it is a storage component
        if (component == null || (!component.getType().equals(ComponentType.BOX_STORAGE) && !component.getType().equals(ComponentType.RED_BOX_STORAGE))) {
            sender.sendMessage("InvalidComponent");
            sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
            return;
        }

        Box box = rewardBoxes.get(idxBox);

        // Checks that reward box is placed correctly in given storage
        if (lostStation.chooseRewardBox(player.getSpaceship(), (BoxStorage) component, idx, box)) {
            sender.sendMessage("BoxChosen");

            rewardBoxes.remove(box);

        } else {
            sender.sendMessage("BoxNotChosen");
        }

        // Checks if all boxes were chosen
        if (rewardBoxes.isEmpty()) {
            sender.sendMessage("EmptyReward");
            leaveStation(player, sender);

        } else {
            sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
        }
    }

    /**
     * Handles the penalty after player left station
     *
     * @author Lorenzo
     * @param player current player
     * @param sender current sender
     * @throws RemoteException
     */
    private void leaveStation(Player player, Sender sender) throws RemoteException {
        if (!phase.equals(EventPhase.CHOOSE_BOX)) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        lostStation.penalty(gameManager.getGame().getBoard(), player);

        sender.sendMessage(new PlayerMovedBackwardMessage(lostStation.getPenaltyDays()));
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedBackwardMessage(player.getName(), lostStation.getPenaltyDays()), sender);

        player.setIsReady(true, gameManager.getGame());
        gameManager.getGameThread().notifyThread();
    }
}