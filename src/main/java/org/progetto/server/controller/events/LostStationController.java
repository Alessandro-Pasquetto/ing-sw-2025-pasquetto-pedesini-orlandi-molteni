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

        if(phase.equals("ASK_FOR_LAND")) {

            if (currPlayer < activePlayers.size()) {
                Player player = activePlayers.get(currPlayer);

                if (player.getSpaceship().getCrewCount() >= lostStation.getRequiredCrew()) {

                    Sender sender = gameManager.getSenderByPlayer(player);

                    sender.sendMessage("LandRequest");

                    phase = "LAND";
                }

            } else {
                phase = "END";
                end();
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
    public void receiveDecisionToLand(Player player, boolean land, Sender sender) throws RemoteException {
        if (phase.equals("LAND")) {

            if (player.equals(activePlayers.get(currPlayer))) {

                if (land) {
                    phase = "CHOOSE_BOX";
                    rewardBoxes = lostStation.getRewardBoxes();

                    gameManager.broadcastGameMessage(new AnotherPlayerLandedMessage(player));
                    sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
                    sender.sendMessage("LandingCompleted");

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
    public void receiveRewardBox(Player player, Box box, int y, int x, int idx, Sender sender) throws RemoteException, IllegalStateException {
        if (phase.equals("CHOOSE_BOX")) {

            if (player.equals(activePlayers.get(currPlayer))) {

                boxChosen = true;

                try {
                    Component[][] matrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                    BoxStorage storage = (BoxStorage) matrix[y][x];

                    lostStation.chooseRewardBox(player.getSpaceship(), storage, idx, box);

                    if (!rewardBoxes.remove(box)) {
                        sender.sendMessage("ChosenBoxNotAvailable");

                    } else if(lostStation.chooseRewardBox(player.getSpaceship(), storage, idx, box)){
                        sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
                        sender.sendMessage("BoxChosen");
                    }

                } catch (ClassCastException e) {
                    throw new IllegalStateException("ComponentIsNotAStorage");

                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new IllegalStateException("ComponentIsNotInMatrix");
                }

                // All the boxes should be chosen or discarded
                if (!rewardBoxes.isEmpty()) {
                    phase = "CHOOSE_BOX";
                } else {
                    phase = "EFFECT";
                    eventEffect();
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Handles the penalty at the end of the rewardBox
     *
     * @author Lorenzo
     */
    private void eventEffect() throws RemoteException {
        if(phase.equals("EFFECT")) {

            if (boxChosen) {
                Player player = activePlayers.get(currPlayer);
                Board board = gameManager.getGame().getBoard();

                // Gets sender reference related to current player
                Sender sender = gameManager.getSenderByPlayer(player);

                lostStation.penalty(gameManager.getGame().getBoard(), player);

                sender.sendMessage(new PlayerMovedBackwardMessage(lostStation.getPenaltyDays()));
                gameManager.broadcastGameMessage(new AnotherPlayerMovedBackwardMessage(player.getName(), lostStation.getPenaltyDays()));

                // Updates turn order
                board.updateTurnOrder();

                // Checks for lapped player
                ArrayList<Player> lappedPlayers = board.checkLappedPlayers();

                if (lappedPlayers != null) {
                    for (Player lappedPlayer : lappedPlayers) {

                        // Gets lapped player sender reference
                        Sender senderLapped = gameManager.getSenderByPlayer(lappedPlayer);

                        senderLapped.sendMessage("YouGotLapped");
                        gameManager.broadcastGameMessageToOthers(new PlayerDefeatedMessage(lappedPlayer.getName()), senderLapped);
                        board.leaveTravel(lappedPlayer);
                    }
                }

                phase = "END";
                end();
            }
        }

        //TODO: aggiungere una classe che si occupa di aggionare in broadcast i parametri della nave
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