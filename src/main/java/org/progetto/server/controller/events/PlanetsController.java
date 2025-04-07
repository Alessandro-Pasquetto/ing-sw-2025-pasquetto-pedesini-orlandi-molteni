package org.progetto.server.controller.events;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.AnotherPlayerMovedAheadMessage;
import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventCommon.AvailableBoxesMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.messages.toClient.Planets.AnotherPlayerLandedMessage;
import org.progetto.messages.toClient.Planets.AvailablePlanetsMessage;
import org.progetto.messages.toClient.PlayerMovedBackwardMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.controller.LobbyController;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.events.Planets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Objects;

public class PlanetsController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================
    private GameManager gameManager;
    private Planets planets;
    private String phase;
    private int currPlayer;
    private int landedPlayers;
    private int leavedPlayers;
    private ArrayList<Player> activePlayers;
    private ArrayList<Box> rewardBoxes;


    // =======================
    // CONSTRUCTORS
    // =======================

    public PlanetsController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.phase = "START";
        this.currPlayer = 0;
        this.landedPlayers = 0;
        this.leavedPlayers = 0;
        this.activePlayers = gameManager.getGame().getBoard().getActivePlayers();
        this.planets = (Planets) gameManager.getGame().getActiveEventCard();
        this.rewardBoxes = new ArrayList<>();
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
     * Ask each player if they want to land on one of the given planets.
     * List of planets are sent only to the active player.
     *
     * @author Lorenzo
     * @throws RemoteException
     */
    private void askForLand() throws RemoteException,IllegalStateException {

        if (phase.equals("ASK_FOR_LAND")) {

            if (currPlayer < activePlayers.size()) {
                Player player = activePlayers.get(currPlayer);

                SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(player);
                VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(player);

                Sender sender = null;

                if (socketWriter != null) {
                    sender = socketWriter;
                } else if (virtualClient != null) {
                    sender = virtualClient;
                }

                sender.sendMessage("LandRequest");
                sender.sendMessage(new AvailablePlanetsMessage( planets.getPlanetsTaken()));

                phase = "LAND";

            } else {
                phase = "END";
                end();
            }
        }
    }

    /**
     * Receive the player decision to land on the planet.
     * Send the available boxes to that player.
     *
     * @author Lorenzo
     * @param player
     * @param land true if the player wants to land
     * @param planetIdx is the index of the planet chosen
     * @param sender
     * @throws RemoteException
     */
    public void receiveDecisionToLand(Player player, boolean land, int planetIdx, Sender sender) throws RemoteException, IllegalStateException {

        if (phase.equals("LAND")) {

            if (player.equals(activePlayers.get(currPlayer))) {

                if (land) {

                    try {
                        if (planets.getPlanetsTaken()[planetIdx]) {
                            sender.sendMessage("PlanetTaken");
                            phase = "ASK_FOR_LAND";

                        } else {
                            planets.choosePlanet(player, planetIdx);
                            landedPlayers++;
                            LobbyController.broadcastLobbyMessage(new AnotherPlayerLandedMessage(player, planetIdx));
                            sender.sendMessage("LandingCompleted");

                            rewardBoxes = planets.getRewardsForPlanets().get(planetIdx);
                            sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
                            sender.sendMessage("AvailableBoxes");

                            phase = "CHOOSE_BOX";
                        }

                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new IllegalStateException("PlanetIndexOutOfBound");
                    }

                } else {
                    phase = "ASK_FOR_LAND";
                    currPlayer++;
                    askForLand();
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * For each player receive the box that the player choose, and it's placement in the component.
     * Update the player's view with the new list of available boxes.
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

            try {
                Component[][] matrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                BoxStorage storage = (BoxStorage) matrix[y][x];

                planets.chooseRewardBox(player.getSpaceship(), storage, idx, box);

                if (!rewardBoxes.remove(box)) {
                    sender.sendMessage("ChosenBoxNotAvailable");

                } else {
                    sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
                    sender.sendMessage("BoxChosen");
                }

            } catch (ClassCastException e) {
                throw new IllegalStateException("ComponentIsNotAStorage");

            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalStateException("ComponentIsNotInMatrix");
            }

            if (!rewardBoxes.isEmpty()) {
                phase = "CHOOSE_BOX";

            } else {
                phase = "LEAVE_PLANET";
                leavePlanet(activePlayers.get(currPlayer), sender);
            }
        }
    }

    /**
     * Function called after all the boxes of a planet are chosen or if the player wants to leave.
     *
     * @author Lorenzo
     * @param player
     * @param sender
     * @throws RemoteException
     * @throws IllegalStateException
     */
    private void leavePlanet(Player player,Sender sender) throws RemoteException,IllegalStateException {
        if (phase.equals("LEAVE_PLANET")) {
            leavedPlayers++;  // next player

            if(leavedPlayers == landedPlayers) {
                phase = "EFFECT";
                eventEffect();
            }
            else{
                currPlayer++;
                phase = "ASK_FOR_LAND";
            }
        }
    }

    /**
     * Calculate the penalty for each landed player.
     *
     * @author Lorenzo
     */
    private void eventEffect() throws RemoteException {
        if (phase.equals("EFFECT")) {

            Board board = gameManager.getGame().getBoard();
            planets.penalty(gameManager.getGame().getBoard());

            for (Player player : planets.getLandedPlayers()){

                SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(player);
                VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(player);

                Sender sender = null;
                if (socketWriter != null) {
                    sender = socketWriter;
                } else if (virtualClient != null) {
                    sender = virtualClient;
                }

                sender.sendMessage(new PlayerMovedBackwardMessage(planets.getPenaltyDays()));
                LobbyController.broadcastLobbyMessage(new AnotherPlayerMovedBackwardMessage(player.getName(), planets.getPenaltyDays()));
            }

            // Updates turn order
            board.updateTurnOrder();

            // Checks for lapped player
            ArrayList<Player> lappedPlayers = board.checkLappedPlayers();

            if (lappedPlayers != null) {
                for (Player lappedPlayer : lappedPlayers) {

                    // Gets lapped player sender reference
                    SocketWriter socketWriterLapped = gameManager.getSocketWriterByPlayer(lappedPlayer);
                    VirtualClient virtualClientLapped = gameManager.getVirtualClientByPlayer(lappedPlayer);

                    Sender senderLapped = null;

                    if (socketWriterLapped != null) {
                        senderLapped = socketWriterLapped;
                    } else if (virtualClientLapped != null) {
                        senderLapped = virtualClientLapped;
                    }

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