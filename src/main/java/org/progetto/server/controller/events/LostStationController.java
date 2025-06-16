package org.progetto.server.controller.events;

import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.messages.toClient.LostStation.AcceptRewardCreditsAndPenaltiesMessage;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.LostStation;

import java.util.ArrayList;

public class LostStationController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private final GameManager gameManager;
    private final LostStation lostStation;
    private final ArrayList<Player> activePlayers;
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
    public void start() throws InterruptedException {
        if (phase.equals(EventPhase.START)) {
            phase = EventPhase.ASK_TO_LAND;
            askForLand();
        }
    }

    /**
     * Ask each player if they want to land on the lost ship, only if the preconditions are satisfied
     *
     * @author Lorenzo
     * @throws InterruptedException
     */
    private void askForLand() throws InterruptedException {
        if (!phase.equals(EventPhase.ASK_TO_LAND))
            throw new IllegalStateException("IncorrectPhase");

        for (Player player : activePlayers) {

            gameManager.getGame().setActivePlayer(player);
            gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

            Sender sender = gameManager.getSenderByPlayer(player);

            if (player.getSpaceship().getTotalCrewCount() >= lostStation.getRequiredCrew()) {
                phase = EventPhase.LAND;
                MessageSenderService.sendMessage("LandRequest", sender);

                gameManager.getGameThread().resetAndWaitTravelerReady(player);

                if(!player.getIsReady() && someoneLanded)
                    leaveStation(player);

            } else {
                MessageSenderService.sendMessage("NotEnoughCrew", sender);
            }

            // Checks if someone landed on the station
            if (someoneLanded) return;
        }
    }

    /**
     * Receive the player decision to land on the lost ship
     * Send the available boxes to that player
     *
     * @author Lorenzo
     * @param player current player
     * @param sender current sender
     * @param decision player's decision
     */
    @Override
    public void receiveDecisionToLand(Player player, String decision, Sender sender) {
        if (!phase.equals(EventPhase.LAND)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        String upperCaseDecision = decision.toUpperCase();

        switch (upperCaseDecision) {
            case "YES":
                phase = EventPhase.CHOOSE_BOX;
                rewardBoxes = lostStation.getRewardBoxes();
                someoneLanded = true;

                MessageSenderService.sendMessage("LandingCompleted", sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerLandedMessage(player), sender);

                MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
                break;

            case "NO":
                player.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();
                break;

            default:
                MessageSenderService.sendMessage("IncorrectResponse", sender);
                MessageSenderService.sendMessage("LandRequest", sender);
                break;
        }
    }

    /**
     * Receive the box that the player choose, and it's placement in the component
     * If player wants to leave he selects idxBox = -1
     *
     * @author Lorenzo
     * @param player that choose the box
     * @param rewardIdxBox chosen
     * @param xBoxStorage coordinate of the component were the box will be placed
     * @param yBoxStorage coordinate of the component were the box will be placed
     * @param idx is where the player want to insert the chosen box
     * @param sender current sender
     */
    @Override
    public void receiveRewardBox(Player player, int rewardIdxBox, int xBoxStorage, int yBoxStorage, int idx, Sender sender) throws IllegalStateException {
        if (!phase.equals(EventPhase.CHOOSE_BOX)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks that current player is trying to get reward the reward box
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        // Checks if reward box index is correct
        if (rewardIdxBox < -1 || rewardIdxBox >= rewardBoxes.size()) {
            MessageSenderService.sendMessage("IncorrectRewardIndex", sender);
            MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
            return;
        }

        // Checks if player wants to leave
        if (rewardIdxBox == -1) {
            leaveStation(player);
            return;
        }

        Component[][] matrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xBoxStorage < 0 || yBoxStorage < 0 || yBoxStorage >= matrix.length || xBoxStorage >= matrix[0].length ) {
            MessageSenderService.sendMessage("InvalidCoordinates", sender);
            MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
            return;
        }

        Component component = matrix[yBoxStorage][xBoxStorage];

        // Checks if it is a storage component
        if (component == null || (!component.getType().equals(ComponentType.BOX_STORAGE) && !component.getType().equals(ComponentType.RED_BOX_STORAGE))) {
            MessageSenderService.sendMessage("InvalidComponent", sender);
            MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
            return;
        }

        Box box = rewardBoxes.get(rewardIdxBox);

        // Checks that reward box is placed correctly in given storage
        try{
            lostStation.chooseRewardBox(player.getSpaceship(), (BoxStorage) component, box, idx);

            rewardBoxes.remove(box);
            gameManager.broadcastGameMessage(new BoxAddedMessage(player.getName(), xBoxStorage, yBoxStorage, idx, box));

        } catch (IllegalStateException e) {
            MessageSenderService.sendMessage(e.getMessage(), sender);
        }

        // Checks if all boxes were chosen
        if (rewardBoxes.isEmpty()) {
            MessageSenderService.sendMessage("EmptyReward", sender);
            leaveStation(player);

        } else {
            MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
        }
    }

    /**
     * Handles the penalty after player left station
     *
     * @author Lorenzo
     * @param player current player
     */
    private void leaveStation(Player player) {

        Sender sender = gameManager.getSenderByPlayer(player);

        lostStation.penalty(gameManager.getGame().getBoard(), player);

        MessageSenderService.sendMessage(new PlayerMovedBackwardMessage(lostStation.getPenaltyDays()), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedBackwardMessage(player.getName(), lostStation.getPenaltyDays()), sender);

        player.setIsReady(true, gameManager.getGame());
        gameManager.getGameThread().notifyThread();
    }

    @Override
    public void reconnectPlayer(Player player, Sender sender) {
        if(!player.equals(gameManager.getGame().getActivePlayer()))
            return;

        if (phase.equals(EventPhase.LAND)){
            MessageSenderService.sendMessage("LandRequest", sender);
        }
        else if (phase.equals(EventPhase.CHOOSE_BOX)){
            MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
        }
    }
}