package org.progetto.server.controller.events;

import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventCommon.AvailableBoxesMessage;
import org.progetto.messages.toClient.EventCommon.BatteriesToDiscardMessage;
import org.progetto.messages.toClient.Planets.AnotherPlayerLandedMessage;
import org.progetto.messages.toClient.Planets.AvailablePlanetsMessage;
import org.progetto.messages.toClient.EventCommon.PlayerMovedBackwardMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.events.Planets;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class PlanetsController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================
    
    private final Planets planets;
    private ArrayList<Box> rewardBoxes;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlanetsController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.phase = EventPhase.START;
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
     * @throws IllegalStateException
     * @throws InterruptedException
     */
    private void askForLand() throws RemoteException, IllegalStateException, InterruptedException {
        if (!phase.equals(EventPhase.ASK_TO_LAND))
            throw new IllegalStateException("IncorrectPhase");

        ArrayList<Player> activePlayers = gameManager.getGame().getBoard().getCopyTravelers();

        for (Player player : activePlayers) {
            gameManager.getGame().setActivePlayer(player);

            Sender sender = gameManager.getSenderByPlayer(player);

            gameManager.broadcastGameMessageToOthers(player.getName() + "'s turn", sender);

            if (planets.getLandedPlayers().size() >= planets.getRewardsForPlanets().size()){
                sender.sendMessage("AllPlanetsAlreadyTaken");
                return;
            }

            // If there is at least a free planet
            phase = EventPhase.LAND;
            sender.sendMessage(new AvailablePlanetsMessage(planets.getPlanetsTaken()));

            gameManager.getGameThread().resetAndWaitPlayerReady(player);
        }

        // Checks that at least a player landed
        if (!planets.getLandedPlayers().isEmpty()) {
            phase = EventPhase.EFFECT;
            eventEffect();
        }
    }

    /**
     * Receive the player decision to land on the planet
     * Send the available boxes to that player
     *
     * @author Gabriele
     * @param player current player
     * @param planetIdx is the index of the planet chosen
     * @param sender current player
     * @throws RemoteException
     * @throws IllegalStateException
     */
    @Override
    public void receiveDecisionToLandPlanet(Player player, int planetIdx, Sender sender) throws RemoteException, IllegalStateException {
        if (!phase.equals(EventPhase.LAND)) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            sender.sendMessage("NotYourTurn");
            return;
        }

        if(planetIdx < -1 || planetIdx >= planets.getRewardsForPlanets().size()){
            sender.sendMessage("PlanetIdxNotValid");
            sender.sendMessage(new AvailablePlanetsMessage(planets.getPlanetsTaken()));
            return;
        }

        if (planetIdx > -1 && planets.getPlanetsTaken()[planetIdx]) {
            sender.sendMessage("PlanetAlreadyTaken");
            sender.sendMessage(new AvailablePlanetsMessage(planets.getPlanetsTaken()));
            return;
        }

        if (planetIdx == -1){
            // If he does not want to land
            phase = EventPhase.ASK_TO_LAND;

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else {
            // If he wants to land
            planets.choosePlanet(player, planetIdx);

            gameManager.broadcastGameMessage(new AnotherPlayerLandedMessage(player, planetIdx));
            sender.sendMessage("LandingCompleted");

            rewardBoxes = planets.getRewardsForPlanets().get(planetIdx);
            sender.sendMessage("AvailableBoxes");
            phase = EventPhase.CHOOSE_BOX;
            sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
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
     * @param sender current sender
     * @throws RemoteException
     * @throws IllegalStateException
     */
    @Override
    public void receiveRewardBox(Player player, int idxBox, int x, int y, int idx, Sender sender) throws RemoteException, IllegalStateException {
        if (!phase.equals(EventPhase.CHOOSE_BOX)) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        // Checks that current player is trying to get reward the reward box
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            sender.sendMessage("NotYourTurn");
            return;
        }

        if(idxBox == -1){
            leavePlanet(player, sender);
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();

        if(x < 0 || y < 0 || x >= spaceshipMatrix[0].length || y >= spaceshipMatrix.length){
            sender.sendMessage("InvalidCoordinates");
            sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
            return;
        }

        Component boxStorage = spaceshipMatrix[y][x];

        if (boxStorage == null || !boxStorage.getType().equals(ComponentType.BOX_STORAGE) && !boxStorage.getType().equals(ComponentType.RED_BOX_STORAGE)) {
            sender.sendMessage("InvalidCoordinates");
            sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
            return;
        }

        // Checks that the box index is valid
        BoxStorage storage = (BoxStorage) boxStorage;
        if(idxBox >= storage.getCapacity() || idxBox<0){
            sender.sendMessage("InvalidStorageIndex");
            sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
            return;
        }

        Box box = rewardBoxes.get(idxBox);

        // Checks that reward box is placed correctly in given storage
        if (!planets.chooseRewardBox(player.getSpaceship(), (BoxStorage) boxStorage, idx, box)) {
            sender.sendMessage("NotValidBoxContainer");
            sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
            return;
        }

        rewardBoxes.remove(idxBox);
        sender.sendMessage("BoxChosen");

        // All the boxes are chosen
        if (rewardBoxes.isEmpty()) {
            sender.sendMessage("EmptyReward");
            leavePlanet(player, sender);

        } else {
            sender.sendMessage(new AvailableBoxesMessage(rewardBoxes));
        }
    }

    /**
     * Function called if the player wants to leave
     *
     * @author Gabriele
     * @param player current player
     * @param sender current sender
     * @throws IllegalStateException
     */
    private void leavePlanet(Player player, Sender sender) throws IllegalStateException, RemoteException {
        if (!phase.equals(EventPhase.CHOOSE_BOX))
            throw new IllegalStateException("IncorrectPhase");

        // Checks that current player is trying to leave planet
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            sender.sendMessage("NotYouTurn");
            return;
        }

        phase = EventPhase.ASK_TO_LAND;

        player.setIsReady(true, gameManager.getGame());
        gameManager.getGameThread().notifyThread();
    }

    /**
     * Calculate the penalty for each landed player
     *
     * @author Lorenzo
     * @throws RemoteException
     */
    private void eventEffect() throws RemoteException {
        if (!phase.equals(EventPhase.EFFECT))
            throw new IllegalStateException("IncorrectPhase");

        for (Player player : planets.getLandedPlayers()){
            Sender sender = gameManager.getSenderByPlayer(player);

            sender.sendMessage(new PlayerMovedBackwardMessage(planets.getPenaltyDays()));
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedBackwardMessage(player.getName(), planets.getPenaltyDays()), sender);
        }

        // Penalty applied
        planets.penalty(gameManager.getGame().getBoard());
    }
}