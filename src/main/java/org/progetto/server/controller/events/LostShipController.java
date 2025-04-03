package org.progetto.server.controller.events;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.*;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.controller.LobbyController;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;
import org.progetto.server.model.events.LostShip;
import org.progetto.server.model.events.Slavers;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class LostShipController {

    // =======================
    // ATTRIBUTES
    // =======================

    private GameManager gameManager;
    private String phase;
    private int currPlayer;
    private ArrayList<Player> activePlayers;
    private int requestedCrew;

    // =======================
    // CONSTRUCTORS
    // =======================

    public LostShipController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.phase = "START";
        this.currPlayer = 0;
        this.activePlayers = gameManager.getGame().getBoard().getActivePlayers();
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
    public void start() throws RemoteException {
        phase = "ASK_TO_LAND";
        askToLand();
    }

    private void askToLand() throws RemoteException {
        if(phase.equals("ASK_TO_LAND")) {
            Player player = activePlayers.get(currPlayer);
            LostShip lostShip = (LostShip) gameManager.getGame().getActiveEventCard();

            SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(player);
            VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(player);

            Sender sender = null;

            if (socketWriter != null) {
                sender = socketWriter;
            } else if (virtualClient != null) {
                sender = virtualClient;
            }

            sender.sendMessage(new AcceptRewardCreditsAndPenalties(lostShip.getRewardCredits(), lostShip.getPenaltyCrew(), lostShip.getPenaltyDays()));
            phase = "REWARD_DECISION";
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
                        phase = "ASK_USE";
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

                LostShip lostShip = (LostShip) gameManager.getGame().getActiveEventCard();
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
     * Receives the coordinates of HousingUnit component from which remove a battery
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
                    Slavers slavers = (Slavers) gameManager.getGame().getActiveEventCard();

                    // Checks if a crew member has been discarded
                    if (slavers.chooseDiscardedCrew(player.getSpaceship(), (HousingUnit) housingUnit)) {
                        requestedCrew--;
                        sender.sendMessage("CrewMemberDiscarded");

                        if (requestedCrew == 0) {
                            phase = "EVENT_EFFECT";
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
        if (phase.equals("EVENT_EFFECT")) {
            Player player = activePlayers.get(currPlayer);
            Slavers slavers = (Slavers) gameManager.getGame().getActiveEventCard();

            // Event effect applied for single player
            slavers.rewardPenalty(gameManager.getGame().getBoard(), player);

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