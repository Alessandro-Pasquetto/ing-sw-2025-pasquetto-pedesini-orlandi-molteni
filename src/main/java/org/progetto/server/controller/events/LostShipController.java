package org.progetto.server.controller.events;

import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
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

    private GameManager gameManager;
    private LostShip lostShip;
    private String phase;
    private int currPlayer;
    private ArrayList<Player> activePlayers;
    private int requestedCrew;

    // =======================
    // CONSTRUCTORS
    // =======================

    public LostShipController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.lostShip = (LostShip) gameManager.getGame().getActiveEventCard();
        this.phase = "START";
        this.currPlayer = 0;
        this.activePlayers = gameManager.getGame().getBoard().getCopyActivePlayers();
        this.requestedCrew = 0;
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

    /**
     * Starts event card effect
     *
     * @author Stefano
     * @throws RemoteException
     */
    @Override
    public void start() throws RemoteException {
        phase = "ASK_TO_LAND";
        askToLand();
    }

    private void askToLand() throws RemoteException {
        if(phase.equals("ASK_TO_LAND")) {
            Player player = activePlayers.get(currPlayer);

            Sender sender = gameManager.getSenderByPlayer(player);

            // Calculates max crew number available to discard
            int maxCrewCount = player.getSpaceship().getTotalCrewCount();

            if (maxCrewCount > lostShip.getPenaltyCrew()) {
                sender.sendMessage(new AcceptRewardCreditsAndPenaltiesMessage(lostShip.getRewardCredits(), lostShip.getPenaltyCrew(), lostShip.getPenaltyDays()));
                phase = "REWARD_DECISION";
            } else {
                sender.sendMessage("NotEnoughCrew");

                // Next player
                if (currPlayer < activePlayers.size()) {
                    currPlayer++;
                    phase = "ASK_TO_LAND";
                    askToLand();
                } else {
                    phase = "END";
                    end();
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
    public void getRewardDecision(Player player, String response, Sender sender) throws RemoteException {
        if (phase.equals("REWARD_DECISION")) {
            String upperCaseResponse = response.toUpperCase();

            switch (upperCaseResponse) {
                case "YES":
                    phase = "PENALTY_EFFECT";
                    break;

                case "NO":
                    // Next player
                    if (currPlayer < activePlayers.size()) {
                        currPlayer++;
                        phase = "ASK_TO_LAND";
                        askToLand();
                    } else {
                        phase = "END";
                        end();
                    }
                    break;

                default:
                    sender.sendMessage("IncorrectResponse");
                    break;
            }
        }
    }

    /**
     * if the player accept, he suffers the penalty
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
                
                requestedCrew = lostShip.getPenaltyCrew();
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
                    if (lostShip.chooseDiscardedCrew(player.getSpaceship(), (HousingUnit) housingUnit)) {
                        requestedCrew--;
                        sender.sendMessage("CrewMemberDiscarded");

                        if (requestedCrew == 0) {
                            phase = "EFFECT";
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
        if (phase.equals("EFFECT")) {
            Player player = activePlayers.get(currPlayer);
            Board board = gameManager.getGame().getBoard();

            // Gets sender reference related to current player
            Sender sender = gameManager.getSenderByPlayer(player);

            // Event effect applied for single player
            lostShip.rewardPenalty(gameManager.getGame().getBoard(), player);

            sender.sendMessage(new PlayerMovedBackwardMessage(lostShip.getPenaltyDays()));
            sender.sendMessage(new PlayerGetsCreditsMessage(lostShip.getRewardCredits()));
            gameManager.broadcastGameMessage(new AnotherPlayerMovedBackwardMessage(player.getName(), lostShip.getPenaltyDays()));
            gameManager.broadcastGameMessage(new AnotherPlayerGetsCreditsMessage(player.getName(), lostShip.getRewardCredits()));

            // Updates turn order
            board.updateTurnOrder();

            // Checks for lapped player
            ArrayList<Player> lappedPlayers = board.checkLappedPlayers();

            if (lappedPlayers != null) {
                for (Player lappedPlayer : lappedPlayers) {

                    // Gets lapped player sender reference
                    Sender senderLapped = gameManager.getSenderByPlayer(player);

                    senderLapped.sendMessage("YouGotLapped");
                    gameManager.broadcastGameMessageToOthers(new PlayerDefeatedMessage(lappedPlayer.getName()), senderLapped);
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
            gameManager.broadcastGameMessage("This event card is finished");
        }
    }
}