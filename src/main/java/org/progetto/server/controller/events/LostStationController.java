package org.progetto.server.controller.events;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.AnotherPlayerMovedAheadMessage;
import org.progetto.messages.toClient.LostShip.AnotherPlayerLandedMessage;
import org.progetto.messages.toClient.LostShip.AvailableBoxesMessage;
import org.progetto.messages.toClient.PlayerMovedAheadMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.controller.LobbyController;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.events.LostStation;
import org.progetto.server.model.events.OpenSpace;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Objects;

public class LostStationController {

    // =======================
    // ATTRIBUTES
    // =======================
    private GameManager gameManager;
    private String phase;
    private int currPlayer;
    private ArrayList<Player> activePlayers;
    private LostStation lostStation;

    boolean box_chosen = false;
    private ArrayList<Box> rewardBoxes;


    // =======================
    // CONSTRUCTORS
    // =======================
    public LostStationController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.phase = "START";
        this.currPlayer = 0;
        this.activePlayers = gameManager.getGame().getBoard().getActivePlayers();
        this.lostStation = null;
        this.rewardBoxes = new ArrayList<>();
    }

    // =======================
    // OTHER METHODS
    // =======================
    public void start() throws RemoteException {
        phase = "ASK_FOR_LAND";
        askForLand();
    }

    /**
     * ask each player if they want to land on the lost ship, only if the precondition
     * are satisfied
     *
     * @author Lorenzo
     * @throws RemoteException
     */
    private void askForLand() throws RemoteException,IllegalStateException {

        if(Objects.equals(phase, "ASK_FOR_LAND")) {

            lostStation = (LostStation) gameManager.getGame().getActiveEventCard();
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
     * receive the player decision to land on the lost ship.
     * send the available boxes to that player.
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
            }
        }
    }

    /**
     * receive the box that the player choose, and it's placement in the component.
     * update the player's view with the new list of available boxes
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
    public void receiveRewardBox(Player player, Box box, int y, int x,int idx, Sender sender) throws RemoteException,IllegalStateException {

        if (Objects.equals(phase, "CHOOSE_BOX")) {

            box_chosen = true;


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

            if (!rewardBoxes.isEmpty()) {    //andrà gestita meglio la sequenza delle fasi
                phase = "CHOOSE_BOX";
            }
        }
    }

    /**
     * called after the end_card message client->server
     *
     * @author Lorenzo
     */
    public void eventEffect() throws RemoteException {   //evento esterno, come gesitsco la sequenza di fasi?

        if(box_chosen) {

            Player player = activePlayers.get(currPlayer);

            lostStation.penalty(gameManager.getGame().getBoard(), player);

            SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(player);
            VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(player);

            Sender sender = null;

            if (socketWriter != null) {
                sender = socketWriter;
            } else if (virtualClient != null) {
                sender = virtualClient;
            }

            sender.sendMessage(new PlayerMovedAheadMessage(lostStation.getPenaltyDays()));
            LobbyController.broadcastLobbyMessage(new AnotherPlayerMovedAheadMessage(player.getName(), lostStation.getPenaltyDays()));

            //todo aggiungere una classe che si occupa di aggionare in broadcast i parametri della nave

        }


    }











}