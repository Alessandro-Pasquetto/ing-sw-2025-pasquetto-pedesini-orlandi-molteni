package org.progetto.server.controller.events;

import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventCommon.AvailableBoxesMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.messages.toClient.Planets.AnotherPlayerLandedMessage;
import org.progetto.messages.toClient.Planets.AvailablePlanetsMessage;
import org.progetto.messages.toClient.PlayerMovedBackwardMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.events.Planets;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class PlanetsController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================
    private GameManager gameManager;
    private Planets planets;
    private String phase;
    private int currPlayer;
    private ArrayList<Player> activePlayers;
    private ArrayList<Box> rewardBoxes;


    // =======================
    // CONSTRUCTORS
    // =======================

    public PlanetsController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.phase = "START";
        this.currPlayer = 0;
        this.activePlayers = gameManager.getGame().getBoard().getCopyActivePlayers();
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
     * Ask each player if they want to land on one of the given planets
     * List of planets are sent only to the active player
     *
     * @author Lorenzo
     * @throws RemoteException
     */
    private void askForLand() throws RemoteException,IllegalStateException {

        if (phase.equals("ASK_FOR_LAND")) {

            if (currPlayer < activePlayers.size()) {
                Player player = activePlayers.get(currPlayer);

                Sender sender = gameManager.getSenderByPlayer(player);

                sender.sendMessage("LandRequest");
                sender.sendMessage(new AvailablePlanetsMessage(planets.getPlanetsTaken()));

                phase = "LAND";

            } else {
                phase = "END";
                end();
            }
        }
    }

    /**
     * Receive the player decision to land on the planet
     * Send the available boxes to that player
     *
     * @author Gabriele
     * @param player
     * @param decision
     * @param planetIdx is the index of the planet chosen
     * @param sender
     * @throws RemoteException
     */
    public void receiveDecisionToLand(Player player, String decision, int planetIdx, Sender sender) throws RemoteException, IllegalStateException {
        if (phase.equals("LAND")) {

            if (player.equals(activePlayers.get(currPlayer))) {

                String upperCaseDecision = decision.toUpperCase();

                switch(upperCaseDecision) {
                    case "YES":
                        try {
                            if (planets.getPlanetsTaken()[planetIdx]) {
                                sender.sendMessage("PlanetAlreadyTaken");
                                phase = "ASK_FOR_LAND";

                            } else {
                                planets.choosePlanet(player, planetIdx);
                                gameManager.broadcastGameMessage(new AnotherPlayerLandedMessage(player, planetIdx));
                                sender.sendMessage("LandingCompleted");

                                rewardBoxes = planets.getRewardsForPlanets().get(planetIdx);
                                sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
                                sender.sendMessage("AvailableBoxes");

                                phase = "CHOOSE_BOX";
                            }

                        } catch (ArrayIndexOutOfBoundsException e) {
                            throw new IllegalStateException("PlanetIndexOutOfBound");
                        }

                    case "NO":
                        phase = "ASK_FOR_LAND";
                        currPlayer++;
                        askForLand();

                    default:
                        sender.sendMessage("IncorrectResponse");
                        break;
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * For each player receive the box that the player choose, and it's placement in the component
     * Update the player's view with the new list of available boxes
     *
     * @author Gabriele
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

            // Checks that current player is trying to get reward the reward box
            if (player.equals(activePlayers.get(currPlayer))) {

                try {
                    Component[][] matrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                    BoxStorage storage = (BoxStorage) matrix[y][x];

                    // Checks box chosen is contained in rewards list
                    if (rewardBoxes.contains(box)) {

                        // Checks that reward box is placed correctly in given storage
                        if (planets.chooseRewardBox(player.getSpaceship(), storage, idx, box)) {
                            sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
                            sender.sendMessage("BoxChosen");

                            rewardBoxes.remove(box);

                        } else {
                            sender.sendMessage("BoxNotChosen");
                        }

                    } else {
                        sender.sendMessage("ChosenBoxNotAvailable");
                    }

                } catch (ClassCastException e) {
                    throw new IllegalStateException("ComponentIsNotAStorage");

                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new IllegalStateException("ComponentIsNotInMatrix");
                }

                // All the boxes are chosen
                if (rewardBoxes.isEmpty()) {
                    leavePlanet(activePlayers.get(currPlayer), sender);
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Function called if the player wants to leave
     *
     * @author Gabriele
     * @param player
     * @param sender
     * @throws RemoteException
     * @throws IllegalStateException
     */
    private void leavePlanet(Player player, Sender sender) throws RemoteException,IllegalStateException {
        if (phase.equals("CHOOSE_BOX")) {

            // Checks that current player is trying to get reward the reward box
            if (player.equals(activePlayers.get(currPlayer))) {

                // Next player
                currPlayer++;

                if (currPlayer < activePlayers.size()) {
                    phase = "ASK_FOR_LAND";
                    askForLand();

                } else {
                    phase = "EFFECT";
                    eventEffect();
                }
            }
        }
    }

    /**
     * Calculate the penalty for each landed player
     *
     * @author Lorenzo
     */
    private void eventEffect() throws RemoteException {
        if (phase.equals("EFFECT")) {

            Board board = gameManager.getGame().getBoard();

            for (Player player : planets.getLandedPlayers()){
                Sender sender = gameManager.getSenderByPlayer(player);

                sender.sendMessage(new PlayerMovedBackwardMessage(planets.getPenaltyDays()));
                gameManager.broadcastGameMessage(new AnotherPlayerMovedBackwardMessage(player.getName(), planets.getPenaltyDays()));
            }

            // Penalty applied
            planets.penalty(gameManager.getGame().getBoard());

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