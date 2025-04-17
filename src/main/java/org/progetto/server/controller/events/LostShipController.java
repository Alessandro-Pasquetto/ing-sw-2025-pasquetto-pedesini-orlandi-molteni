package org.progetto.server.controller.events;

import org.progetto.messages.toClient.EventCommon.*;
import org.progetto.messages.toClient.LostStation.AcceptRewardCreditsAndPenaltiesMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
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

    private LostShip lostShip;
    private ArrayList<Player> activePlayers;
    private int requestedCrew;

    // =======================
    // CONSTRUCTORS
    // =======================

    public LostShipController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.lostShip = (LostShip) gameManager.getGame().getActiveEventCard();
        this.phase = EventPhase.START;
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();
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
    public void start() throws RemoteException, InterruptedException {
        phase = EventPhase.ASK_TO_LAND;
        askToLand();
    }

    private void askToLand() throws RemoteException, InterruptedException {
        if(phase.equals(EventPhase.ASK_TO_LAND)) {

            for (Player player : activePlayers) {

                gameManager.getGame().setActivePlayer(player);

                Sender sender = gameManager.getSenderByPlayer(player);

                // Calculates max crew number available to discard
                int maxCrewCount = player.getSpaceship().getTotalCrewCount();

                if (maxCrewCount > lostShip.getPenaltyCrew()) {
                    sender.sendMessage(new AcceptRewardCreditsAndPenaltiesMessage(lostShip.getRewardCredits(), lostShip.getPenaltyCrew(), lostShip.getPenaltyDays()));
                    phase = EventPhase.REWARD_DECISION;
                    gameManager.getGameThread().resetAndWaitPlayerReady(player);

                } else {
                    sender.sendMessage("NotEnoughCrew");
                }
            }
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
    @Override
    public void receiveRewardAndPenaltiesDecision(Player player, String response, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.REWARD_DECISION)) {

            if (player.equals(gameManager.getGame().getActivePlayer())) {
                String upperCaseResponse = response.toUpperCase();

                switch (upperCaseResponse) {
                    case "YES":
                        phase = EventPhase.PENALTY_EFFECT;
                        penaltyEffect(player, sender);
                        break;

                    case "NO":
                        phase = EventPhase.ASK_TO_LAND;

                        player.setIsReady(true, gameManager.getGame());
                        gameManager.getGameThread().notifyThread();
                        break;

                    default:
                        sender.sendMessage("IncorrectResponse");
                        break;
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase: " + phase);
        }
    }

    /**
     * If the player accept, he suffers the penalty
     *
     * @author Stefano
     * @param player
     * @param sender
     * @throws RemoteException
     */
    public void penaltyEffect(Player player, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.PENALTY_EFFECT)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                requestedCrew = lostShip.getPenaltyCrew();
                sender.sendMessage(new CrewToDiscardMessage(requestedCrew));
                phase = EventPhase.DISCARDED_CREW;

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
    @Override
    public void receiveDiscardedCrew(Player player, int xHousingUnit, int yHousingUnit, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.DISCARDED_CREW)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();
                Component housingUnit = spaceshipMatrix[yHousingUnit][xHousingUnit];

                if (housingUnit != null && housingUnit.getType().equals(ComponentType.HOUSING_UNIT)) {

                    // Checks if a crew member has been discarded
                    if (lostShip.chooseDiscardedCrew(player.getSpaceship(), (HousingUnit) housingUnit)) {
                        requestedCrew--;
                        sender.sendMessage("CrewMemberDiscarded");

                        if (requestedCrew == 0) {
                            phase = EventPhase.EFFECT;
                            eventEffect();

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
     * If the player accepted, he receives the reward and loses the penalty days
     *
     * @author Stefano
     * @throws RemoteException
     */
    private void eventEffect() throws RemoteException {
        if (phase.equals(EventPhase.EFFECT)) {
            Player player = gameManager.getGame().getActivePlayer();
            Board board = gameManager.getGame().getBoard();

            // Gets sender reference related to current player
            Sender sender = gameManager.getSenderByPlayer(player);

            // Event effect applied for single player
            lostShip.rewardPenalty(gameManager.getGame().getBoard(), player);

            sender.sendMessage(new PlayerMovedBackwardMessage(lostShip.getPenaltyDays()));
            sender.sendMessage(new PlayerGetsCreditsMessage(lostShip.getRewardCredits()));
            gameManager.broadcastGameMessage(new AnotherPlayerMovedBackwardMessage(player.getName(), lostShip.getPenaltyDays()));
            gameManager.broadcastGameMessage(new AnotherPlayerGetsCreditsMessage(player.getName(), lostShip.getRewardCredits()));
        }
    }
}