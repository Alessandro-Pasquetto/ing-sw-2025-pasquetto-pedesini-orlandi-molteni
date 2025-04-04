package org.progetto.server.controller.events;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.AnotherPlayerMovedAheadMessage;
import org.progetto.messages.toClient.EventCommon.AvailableBoxesMessage;
import org.progetto.messages.toClient.Planets.AnotherPlayerLandedMessage;
import org.progetto.messages.toClient.Planets.AvailablePlanetsMessage;
import org.progetto.messages.toClient.PlayerMovedBackwardMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.controller.LobbyController;
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
    private String phase;
    private int currPlayer;
    private int landedPlayers;
    private int leavedPlayers;
    private ArrayList<Player> activePlayers;
    private Planets planets;

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
     * ask each player if they want to land on one of the given planets.
     * list of planets are sent only to the active player.
     *
     * @author Lorenzo
     * @throws RemoteException
     */
    private void askForLand() throws RemoteException,IllegalStateException {

        if(Objects.equals(phase, "ASK_FOR_LAND")) {

            try {

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


            }catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalStateException("AllPlayersChecked");
            }
        }
    }


    /**
     * receive the player decision to land on the planet.
     * send the available boxes to that player.
     *
     * @author Lorenzo
     * @param player
     * @param land true if the player wants to land
     * @param planetIdx is the index of the planet chosen
     * @param sender
     * @throws RemoteException
     */
    public void receiveDecisionToLand(Player player,boolean land, int planetIdx,Sender sender) throws RemoteException,IllegalStateException {

        if (Objects.equals(phase, "LAND")) {
            if(land) {
              try {
                  if(planets.getPlanetsTaken()[planetIdx]) {
                      sender.sendMessage("planetsTaken");
                      phase = "ASK_FOR_LAND";
                  }
                  else {

                      planets.choosePlanet(player, planetIdx);
                      landedPlayers++;
                      LobbyController.broadcastLobbyMessage(new AnotherPlayerLandedMessage(player,planetIdx));
                      sender.sendMessage("LandedCompeted");

                      rewardBoxes = planets.getRewardsForPlanets().get(planetIdx);
                      sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
                      sender.sendMessage("AvailableBoxes");

                      phase = "CHOOSE_BOX";

                  }

              }catch(ArrayIndexOutOfBoundsException e){
                  throw new IllegalStateException("PlanetIndexOutOfBound");
              }

            }
            else {
                phase = "ASK_FOR_LAND";
                currPlayer ++;
            }
        }
    }


    /**
     * for each player receive the box that the player choose, and it's placement in the component.
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
            }
            else{
                phase = "LEAVE_PLANET";
                leavePlanet(activePlayers.get(currPlayer),sender);
            }
        }
    }

    /**
     * function called after all the boxes of a planet are chosen or if the
     * player wants to leave
     *
     * @param player
     * @param sender
     * @throws RemoteException
     * @throws IllegalStateException
     */
    private void leavePlanet(Player player,Sender sender) throws RemoteException,IllegalStateException {
        if (Objects.equals(phase, "LEAVE_PLANET")) {
            leavedPlayers++;//next player

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
     * calculate the penalty foreach landed player
     *
     * @author Lorenzo
     */
    private void eventEffect() throws RemoteException {
        if(Objects.equals(phase, "EFFECT")) {

            planets.penalty(gameManager.getGame().getBoard());
            for(Player player : planets.getLandedPlayers()){

                SocketWriter socketWriter = gameManager.getSocketWriterByPlayer(player);
                VirtualClient virtualClient = gameManager.getVirtualClientByPlayer(player);

                Sender sender = null;
                if (socketWriter != null) {
                    sender = socketWriter;
                } else if (virtualClient != null) {
                    sender = virtualClient;
                }

                sender.sendMessage(new PlayerMovedBackwardMessage(planets.getPenaltyDays()));
                LobbyController.broadcastLobbyMessage(new AnotherPlayerMovedAheadMessage(player.getName(), planets.getPenaltyDays()));
            }
        }
    }
}