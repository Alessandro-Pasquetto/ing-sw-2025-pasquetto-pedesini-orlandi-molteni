package org.progetto.server.controller.events;

import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.messages.toClient.LostStation.AcceptRewardCreditsAndPenaltiesMessage;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipMessage;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;
import org.progetto.server.model.events.LostShip;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class LostShipController extends EventControllerAbstract  {

    // =======================
    // ATTRIBUTES
    // =======================

    private final LostShip lostShip;
    private final ArrayList<Player> activePlayers;
    private int requestedCrew;
    private final ArrayList<HousingUnit> housingUnits;

    // =======================
    // CONSTRUCTORS
    // =======================

    public LostShipController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.lostShip = (LostShip) gameManager.getGame().getActiveEventCard();
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
     * @throws RemoteException
     */
    @Override
    public void start() throws RemoteException, InterruptedException {
        phase = EventPhase.ASK_TO_LAND;
        askToLand();
    }

    /**
     * Asks to land on the ship
     *
     * @author Gabriele
     * @throws RemoteException
     * @throws InterruptedException
     */
    private void askToLand() throws RemoteException, InterruptedException {
        if(!phase.equals(EventPhase.ASK_TO_LAND))
            throw new IllegalStateException("IncorrectPhase");

        for (Player player : activePlayers) {

            gameManager.getGame().setActivePlayer(player);
            housingUnits.clear();

            Sender sender = gameManager.getSenderByPlayer(player);

            // Calculates max crew number available to discard
            int maxCrewCount = player.getSpaceship().getTotalCrewCount();

            if (maxCrewCount > lostShip.getPenaltyCrew()) {
                phase = EventPhase.REWARD_DECISION;
                sender.sendMessage(new AcceptRewardCreditsAndPenaltiesMessage(lostShip.getRewardCredits(), lostShip.getPenaltyCrew(), lostShip.getPenaltyDays()));

                gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                gameManager.getGameThread().resetAndWaitTravelerReady(player);

                // If the player is not disconnected
                if(player.getIsReady()){
                    if(!housingUnits.isEmpty()){
                        for (HousingUnit housingUnit : housingUnits) {
                            lostShip.chooseDiscardedCrew(player.getSpaceship(), housingUnit);
                        }

                        // Update spaceship to remove highlight components when it's not my turn.
                        // For others, it's used to reload the spaceship in case they got disconnected while it was discarding.
                        if(!housingUnits.isEmpty())
                            gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));

                        phase = EventPhase.EFFECT;
                        eventEffect();
                        return;
                    }

                }else{
                    player.setIsReady(true, gameManager.getGame());
                }

            } else {
                sender.sendMessage("NotEnoughCrew");
            }
        }
    }

    /**
     * Receives response for rewardPenalty
     *
     * @author Stefano
     * @param player current player
     * @param response player's response
     * @param sender current sender
     * @throws RemoteException
     */
    @Override
    public void receiveRewardAndPenaltiesDecision(Player player, String response, Sender sender) throws RemoteException {
        if (!phase.equals(EventPhase.REWARD_DECISION)) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        // Checks if active player is correct
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            sender.sendMessage("NotYourTurn");
            return;
        }

        String upperCaseResponse = response.toUpperCase();

        switch (upperCaseResponse) {
            case "YES":
                phase = EventPhase.PENALTY_EFFECT;
                penaltyEffect(player, sender);
                break;

            case "NO":
                player.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();
                break;

            default:
                sender.sendMessage("IncorrectResponse");
                sender.sendMessage(new AcceptRewardCreditsAndPenaltiesMessage(lostShip.getRewardCredits(), lostShip.getPenaltyCrew(), lostShip.getPenaltyDays()));
                break;
        }
    }

    /**
     * If the player accept, he suffers the penalty
     *
     * @author Stefano
     * @param player current player
     * @param sender current sender
     * @throws RemoteException
     */
    private void penaltyEffect(Player player, Sender sender) throws RemoteException {
        if (!phase.equals(EventPhase.PENALTY_EFFECT)) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            sender.sendMessage("NotYourTurn");
            return;
        }

        requestedCrew = lostShip.getPenaltyCrew();
        phase = EventPhase.DISCARDED_CREW;
        sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
    }

    /**
     * Receives the coordinates of HousingUnit component from which remove a crew member
     *
     * @author Stefano
     * @param player current player
     * @param xHousingUnit x coordinate of chosen housing unit
     * @param yHousingUnit y coordinate of chosen housing unit
     * @param sender current sender
     * @throws RemoteException
     */
    @Override
    public void receiveDiscardedCrew(Player player, int xHousingUnit, int yHousingUnit, Sender sender) throws RemoteException {
        if (!phase.equals(EventPhase.DISCARDED_CREW)) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            sender.sendMessage("NotYourTurn");
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xHousingUnit < 0 || yHousingUnit < 0 || yHousingUnit >= spaceshipMatrix.length || xHousingUnit >= spaceshipMatrix[0].length ) {
            sender.sendMessage("InvalidCoordinates");
            sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
            return;
        }

        Component housingUnitComp = spaceshipMatrix[yHousingUnit][xHousingUnit];

        if (housingUnitComp == null || (!housingUnitComp.getType().equals(ComponentType.HOUSING_UNIT) && !housingUnitComp.getType().equals(ComponentType.CENTRAL_UNIT))) {
            sender.sendMessage("InvalidComponent");
            sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
            return;
        }

        HousingUnit housingUnit = (HousingUnit) housingUnitComp;

        // Checks if a crew member has been discarded
        try{
            housingUnits.add(housingUnit);
            requestedCrew--;

            sender.sendMessage(new CrewDiscardedMessage(xHousingUnit, yHousingUnit));
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerCrewDiscardedMessage(player.getName(), xHousingUnit, yHousingUnit), sender);

            if (requestedCrew == 0) {
                player.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();
            } else
                sender.sendMessage(new CrewToDiscardMessage(requestedCrew));

        }catch (IllegalStateException e){
            sender.sendMessage("CrewMemberNotDiscarded");
            sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
        }
    }

    /**
     * If the player accepted, he receives the reward and loses the penalty days
     *
     * @author Stefano
     * @throws RemoteException
     */
    private void eventEffect() throws RemoteException {
        if (phase.equals(EventPhase.EFFECT)) {
            Player player = gameManager.getGame().getActivePlayer();

            // Gets sender reference related to current player
            Sender sender = gameManager.getSenderByPlayer(player);

            // Event effect applied for single player
            lostShip.rewardPenalty(gameManager.getGame().getBoard(), player);

            sender.sendMessage(new PlayerMovedBackwardMessage(lostShip.getPenaltyDays()));
            sender.sendMessage(new PlayerGetsCreditsMessage(lostShip.getRewardCredits()));
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedBackwardMessage(player.getName(), lostShip.getPenaltyDays()), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerGetsCreditsMessage(player.getName(), lostShip.getRewardCredits()), sender);

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
        }
    }
}