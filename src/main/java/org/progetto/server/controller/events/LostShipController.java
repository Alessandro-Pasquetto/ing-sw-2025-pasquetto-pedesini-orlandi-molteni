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
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;
import org.progetto.server.model.events.LostShip;

import java.util.ArrayList;

public class LostShipController extends EventControllerAbstract  {

    // =======================
    // ATTRIBUTES
    // =======================

    private final LostShip lostShip;
    private boolean someoneLanded;
    private final ArrayList<Player> activePlayers;
    private int requestedCrew;
    private final ArrayList<HousingUnit> housingUnits;

    // =======================
    // CONSTRUCTORS
    // =======================

    public LostShipController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.lostShip = (LostShip) gameManager.getGame().getActiveEventCard();
        this.someoneLanded = false;
        this.phase = EventPhase.START;
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();
        this.requestedCrew = 0;
        this.housingUnits = new ArrayList<>();
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Starts event card effect
     *
     * @author Stefano
     */
    @Override
    public void start() throws InterruptedException {
        phase = EventPhase.ASK_TO_LAND;
        askToLand();
    }

    @Override
    public boolean isParticipant(Player player){
        return activePlayers.contains(player);
    }

    /**
     * Asks to land on the ship
     *
     * @author Gabriele
     */
    private void askToLand() throws InterruptedException {
        if(!phase.equals(EventPhase.ASK_TO_LAND))
            throw new IllegalStateException("IncorrectPhase");

        for (Player player : activePlayers) {

            gameManager.getGame().setActivePlayer(player);
            gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

            Sender sender = gameManager.getSenderByPlayer(player);

            // Calculates max crew number available to discard
            int maxCrewCount = player.getSpaceship().getTotalCrewCount();

            if (maxCrewCount > lostShip.getPenaltyCrew()) {

                phase = EventPhase.REWARD_DECISION;
                MessageSenderService.sendMessage(new AcceptRewardCreditsAndPenaltiesMessage(lostShip.getRewardCredits(), lostShip.getPenaltyCrew(), lostShip.getPenaltyDays()), sender);

                gameManager.getGameThread().resetAndWaitTravelerReady(player);

                // Update spaceship to remove highlight components
                // For others, it's used to reload the spaceship in case of disconnections while he was discarding.
                gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));

            } else
                MessageSenderService.sendMessage("NotEnoughCrew", sender);

            // Checks if someone landed on the ship
            if (someoneLanded) return;
        }

        // Reset activePlayer
        gameManager.getGame().setActivePlayer(null);
    }

    /**
     * Receives response for rewardPenalty
     *
     * @author Stefano
     * @param player current player
     * @param response player's response
     * @param sender current sender
     */
    @Override
    public void receiveRewardAndPenaltiesDecision(Player player, String response, Sender sender) {
        if (!phase.equals(EventPhase.REWARD_DECISION)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if active player is correct
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        String upperCaseResponse = response.toUpperCase();

        switch (upperCaseResponse) {
            case "YES":
                phase = EventPhase.PENALTY_EFFECT;
                sendPenaltyEffect(sender);
                break;

            case "NO":
                player.setIsReady(true);
                gameManager.getGameThread().notifyThread();
                break;

            default:
                MessageSenderService.sendMessage("IncorrectResponse", sender);
                MessageSenderService.sendMessage(new AcceptRewardCreditsAndPenaltiesMessage(lostShip.getRewardCredits(), lostShip.getPenaltyCrew(), lostShip.getPenaltyDays()), sender);
                break;
        }
    }

    /**
     * If the player accept, he suffers the penalty
     *
     * @author Stefano
     * @param sender current sender
     */
    private void sendPenaltyEffect(Sender sender) {
        if (!phase.equals(EventPhase.PENALTY_EFFECT))
            throw new IllegalStateException("IncorrectPhase");

        requestedCrew = lostShip.getPenaltyCrew();

        housingUnits.clear();

        phase = EventPhase.DISCARDED_CREW;
        MessageSenderService.sendMessage(new CrewToDiscardMessage(requestedCrew), sender);
    }

    /**
     * Receives the coordinates of HousingUnit component from which remove a crew member
     *
     * @author Stefano
     * @param player current player
     * @param xHousingUnit x coordinate of chosen housing unit
     * @param yHousingUnit y coordinate of chosen housing unit
     * @param sender current sender
     */
    @Override
    public void receiveDiscardedCrew(Player player, int xHousingUnit, int yHousingUnit, Sender sender) {
        if (!phase.equals(EventPhase.DISCARDED_CREW)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xHousingUnit < 0 || yHousingUnit < 0 || yHousingUnit >= spaceshipMatrix.length || xHousingUnit >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendMessage("InvalidCoordinates", sender);
            MessageSenderService.sendMessage(new CrewToDiscardMessage(requestedCrew), sender);
            return;
        }

        Component housingUnitComp = spaceshipMatrix[yHousingUnit][xHousingUnit];

        if (housingUnitComp == null || (!housingUnitComp.getType().equals(ComponentType.HOUSING_UNIT) && !housingUnitComp.getType().equals(ComponentType.CENTRAL_UNIT))) {
            MessageSenderService.sendMessage("InvalidComponent", sender);
            MessageSenderService.sendMessage(new CrewToDiscardMessage(requestedCrew), sender);
            return;
        }

        HousingUnit housingUnit = (HousingUnit) housingUnitComp;

        if(housingUnit.getCrewCount() == 0) {
            MessageSenderService.sendMessage("EmptyHousingUnit", sender);
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedCrew), sender);
            return;
        }

        housingUnits.add(housingUnit);
        requestedCrew--;

        MessageSenderService.sendMessage(new CrewDiscardedMessage(xHousingUnit, yHousingUnit), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerCrewDiscardedMessage(player.getName(), xHousingUnit, yHousingUnit), sender);

        if (requestedCrew == 0) {

            for (HousingUnit component : housingUnits) {
                lostShip.chooseDiscardedCrew(player.getSpaceship(), component);
            }

            phase = EventPhase.EFFECT;
            eventEffect();
        } else
            MessageSenderService.sendMessage(new CrewToDiscardMessage(requestedCrew), sender);
    }

    /**
     * If the player accepted, he receives the reward and loses the penalty days
     *
     * @author Stefano
     */
    private void eventEffect() {
        if (!phase.equals(EventPhase.EFFECT))
            throw new IllegalStateException("IncorrectPhase");

        Player player = gameManager.getGame().getActivePlayer();

        // Gets sender reference related to current player
        Sender sender = gameManager.getSenderByPlayer(player);

        // Event effect applied for single player
        lostShip.rewardPenalty(gameManager.getGame().getBoard(), player);

        MessageSenderService.sendMessage(new PlayerMovedBackwardMessage(lostShip.getPenaltyDays()), sender);
        MessageSenderService.sendMessage(new PlayerGetsCreditsMessage(lostShip.getRewardCredits()), sender);

        gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedBackwardMessage(player.getName(), lostShip.getPenaltyDays()), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerGetsCreditsMessage(player.getName(), lostShip.getRewardCredits()), sender);

        someoneLanded = true;

        player.setIsReady(true);
        gameManager.getGameThread().notifyThread();
    }

    @Override
    public void reconnectPlayer(Player player, Sender sender) {
        if(!player.equals(gameManager.getGame().getActivePlayer()))
            return;

        if (phase.equals(EventPhase.REWARD_DECISION)){
            MessageSenderService.sendMessage(new AcceptRewardCreditsAndPenaltiesMessage(lostShip.getRewardCredits(), lostShip.getPenaltyCrew(), lostShip.getPenaltyDays()), sender);
        }
        else if (phase.equals(EventPhase.DISCARDED_CREW)){

            // Remove batteries already discarded
            for(HousingUnit housingUnit : housingUnits){
                MessageSenderService.sendMessage(new CrewDiscardedMessage(housingUnit.getX(), housingUnit.getY()), sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerCrewDiscardedMessage(player.getName(), housingUnit.getX(), housingUnit.getY()), sender);
            }

            MessageSenderService.sendMessage(new CrewToDiscardMessage(requestedCrew), sender);
        }
    }
}