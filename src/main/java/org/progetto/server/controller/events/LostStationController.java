package org.progetto.server.controller.events;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.messages.toClient.LostShip.AnotherPlayerLandedMessage;
import org.progetto.messages.toClient.EventCommon.AvailableBoxesMessage;
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
import org.progetto.server.model.events.LostStation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Objects;

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
        this.activePlayers = gameManager.getGame().getBoard().getActivePlayers();
        this.lostStation = (LostStation) gameManager.getGame().getActiveEventCard();
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
     * Ask each player if they want to land on the lost ship, only if the preconditions are satisfied.
     *
     * @author Lorenzo
     * @throws RemoteException
     */
    private void askForLand() throws RemoteException,IllegalStateException {

        if(Objects.equals(phase, "ASK_FOR_LAND")) {

            try {
                Player player = activePlayers.get(currPlayer);

                if (player.getSpaceship().getCrewCount() >= lostStation.getRequiredCrew()) {

                    SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(player);
                    VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(player);

                    Sender sender = null;

                    if (socketWriter != null) {
                        sender = socketWriter;
                    } else if (virtualClient != null) {
                        sender = virtualClient;
                    }

                    sender.sendMessage("LandRequest");

                    phase = "LAND";
                }

            }catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalStateException("AllPlayersChecked");
            }
        }
    }


    /**
     * Receive the player decision to land on the lost ship.
     * Send the available boxes to that player.
     *
     * @author Lorenzo
     * @param player
     * @param sender
     * @param land
     * @throws RemoteException
     */
    public void receiveDecisionToLand(Player player,boolean land,Sender sender) throws RemoteException{

        if (Objects.equals(phase, "LAND")) {

            if(land) {
                phase = "CHOOSE_BOX";
                rewardBoxes = lostStation.getRewardBoxes();

                LobbyController.broadcastLobbyMessage(new AnotherPlayerLandedMessage(player));
                sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
                sender.sendMessage("LandedCompeted");
            }
            else {
                phase = "ASK_FOR_LAND";
                currPlayer ++;
                askForLand();
            }
        }
    }

    /**
     * Receive the box that the player choose, and it's placement in the component.
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
    public void receiveRewardBox(Player player, Box box, int y, int x,int idx, Sender sender) throws RemoteException, IllegalStateException {

        if (Objects.equals(phase, "CHOOSE_BOX")) {

            boxChosen = true;

            try {
                Component[][] matrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                BoxStorage storage = (BoxStorage) matrix[y][x];

                lostStation.chooseRewardBox(player.getSpaceship(), storage, idx, box);

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

            if (!rewardBoxes.isEmpty()) {    //tutte le box devono essere scelte o scartate
                phase = "CHOOSE_BOX";
            }
            else{
                phase = "EFFECT";
                eventEffect();
            }
        }
    }

    /**
     * Handles the penalty at the end of the rewardBox
     *
     * @author Lorenzo
     */
    private void eventEffect() throws RemoteException {
        if(Objects.equals(phase, "EFFECT")) {

            if (boxChosen) {
                Player player = activePlayers.get(currPlayer);
                Board board = gameManager.getGame().getBoard();

                // Gets sender reference related to current player
                SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(player);
                VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(player);

                Sender sender = null;

                if (socketWriter != null) {
                    sender = socketWriter;
                } else if (virtualClient != null) {
                    sender = virtualClient;
                }

                lostStation.penalty(gameManager.getGame().getBoard(), player);

                sender.sendMessage(new PlayerMovedBackwardMessage(lostStation.getPenaltyDays()));
                LobbyController.broadcastLobbyMessage(new AnotherPlayerMovedBackwardMessage(player.getName(), lostStation.getPenaltyDays()));

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

        //todo: aggiungere una classe che si occupa di aggionare in broadcast i parametri della nave
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