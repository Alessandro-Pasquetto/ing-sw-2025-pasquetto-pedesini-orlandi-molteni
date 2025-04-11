package org.progetto.server.controller.events;

import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventCommon.AvailableBoxesMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.messages.toClient.Planets.AnotherPlayerLandedMessage;
import org.progetto.messages.toClient.Planets.AvailablePlanetsMessage;
import org.progetto.messages.toClient.PlayerMovedBackwardMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
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
    
    private Planets planets;
    private ArrayList<Player> activePlayers;
    private ArrayList<Box> rewardBoxes;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlanetsController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.phase = EventPhase.START;
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();
        this.planets = (Planets) gameManager.getGame().getActiveEventCard();
        this.rewardBoxes = new ArrayList<>();
    }

    // =======================
    // OTHER METHODS
    // =======================

    @Override
    public void start() throws RemoteException, InterruptedException {
        if(phase.equals(EventPhase.START)){
            phase = EventPhase.ASK_TO_LAND;
            askForLand();
        }
    }

    /**
     * Ask each player if they want to land on one of the given planets
     * List of planets are sent only to the active player
     *
     * @author Lorenzo
     * @throws RemoteException
     */
    private void askForLand() throws RemoteException, IllegalStateException, InterruptedException {
        if (phase.equals(EventPhase.ASK_TO_LAND)) {

            for (Player player : activePlayers) {

                gameManager.getGame().setActivePlayer(player);

                Sender sender = gameManager.getSenderByPlayer(player);

                // Checks if there is at least a free planet
                if (planets.getLandedPlayers().size() < planets.getRewardsForPlanets().size()) {
                    sender.sendMessage("LandRequest");
                    sender.sendMessage(new AvailablePlanetsMessage(planets.getPlanetsTaken()));
                    phase = EventPhase.LAND;

                    gameManager.getGameThread().waitPlayerReady(player);

                } else {
                    sender.sendMessage("AllPlanetsAlreadyTaken");
                    break;
                }
            }

            // Checks that at least a player landed
            if (!planets.getLandedPlayers().isEmpty()) {
                phase = EventPhase.EFFECT;
                eventEffect();
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
    public void receiveDecisionToLand(Player player, String decision, int planetIdx, Sender sender) throws RemoteException, IllegalStateException, InterruptedException {
        if (phase.equals(EventPhase.LAND)) {

            if (player.equals(gameManager.getGame().getActivePlayer())) {

                String upperCaseDecision = decision.toUpperCase();

                switch(upperCaseDecision) {
                    case "YES":
                        try {
                            if (planets.getPlanetsTaken()[planetIdx]) {
                                sender.sendMessage("PlanetAlreadyTaken");

                            } else {
                                planets.choosePlanet(player, planetIdx);

                                gameManager.broadcastGameMessage(new AnotherPlayerLandedMessage(player, planetIdx));
                                sender.sendMessage("LandingCompleted");

                                rewardBoxes = planets.getRewardsForPlanets().get(planetIdx);
                                sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
                                sender.sendMessage("AvailableBoxes");

                                phase = EventPhase.CHOOSE_BOX;
                            }

                        } catch (ArrayIndexOutOfBoundsException e) {
                            sender.sendMessage("PlanetIndexOutOfBound");
                        }

                    case "NO":
                        phase = EventPhase.ASK_TO_LAND;

                        player.setIsReady(true, gameManager.getGame());
                        gameManager.getGameThread().notifyThread();

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
     * @param idxBox chosen
     * @param y coordinate of the component were the box will be placed
     * @param x coordinate of the component were the box will be placed
     * @param idx is where the player want to insert the chosen box
     * @param sender
     * @throws RemoteException
     */
    public void receiveRewardBox(Player player, int idxBox, int y, int x, int idx, Sender sender) throws RemoteException, IllegalStateException {
        if (phase.equals(EventPhase.CHOOSE_BOX)) {

            // Checks that current player is trying to get reward the reward box
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                try {
                    Component[][] matrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();
                    BoxStorage storage = (BoxStorage) matrix[y][x];
                    Box box = rewardBoxes.get(idxBox);

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
                    leavePlanet(player, sender);
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
        if (phase.equals(EventPhase.CHOOSE_BOX)) {

            // Checks that current player is trying to leave planet
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                phase = EventPhase.ASK_TO_LAND;

                player.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();
            }
        }
    }

    /**
     * Calculate the penalty for each landed player
     *
     * @author Lorenzo
     */
    private void eventEffect() throws RemoteException {
        if (phase.equals(EventPhase.EFFECT)) {

            Board board = gameManager.getGame().getBoard();

            for (Player player : planets.getLandedPlayers()){
                Sender sender = gameManager.getSenderByPlayer(player);

                sender.sendMessage(new PlayerMovedBackwardMessage(planets.getPenaltyDays()));
                gameManager.broadcastGameMessage(new AnotherPlayerMovedBackwardMessage(player.getName(), planets.getPenaltyDays()));
            }

            // Penalty applied
            planets.penalty(gameManager.getGame().getBoard());
        }
    }
}