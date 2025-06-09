package org.progetto.server.controller.events;

import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.messages.toClient.Planets.AnotherPlayerLandedPlanetMessage;
import org.progetto.messages.toClient.Planets.AvailablePlanetsMessage;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.events.Planets;

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
    public void start() throws InterruptedException {
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
     * @throws IllegalStateException
     * @throws InterruptedException
     */
    private void askForLand() throws IllegalStateException, InterruptedException {
        if (!phase.equals(EventPhase.ASK_TO_LAND))
            throw new IllegalStateException("IncorrectPhase");

        ArrayList<Player> activePlayers = gameManager.getGame().getBoard().getCopyTravelers();

        for (Player player : activePlayers) {
            gameManager.getGame().setActivePlayer(player);
            gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

            Sender sender = gameManager.getSenderByPlayer(player);

            if (planets.getLandedPlayers().size() >= planets.getRewardsForPlanets().size()){
                MessageSenderService.sendMessage("AllPlanetsAlreadyTaken", sender);
                return;
            }

            // If there is at least a free planet
            phase = EventPhase.LAND;
            MessageSenderService.sendMessage(new AvailablePlanetsMessage(planets.getRewardsForPlanets(), planets.getPlanetsTaken()), sender);

            gameManager.getGameThread().resetAndWaitTravelerReady(player);

            // If it disconnects
            if(!player.getIsReady()){
                player.setIsReady(true, gameManager.getGame());
            }
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
     * @author Lorenzo
     * @param player current player
     * @param planetIdx is the index of the planet chosen
     * @param sender current player
     * @throws IllegalStateException
     */
    @Override
    public void receiveDecisionToLandPlanet(Player player, int planetIdx, Sender sender) throws IllegalStateException {
        if (!phase.equals(EventPhase.LAND)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        if(planetIdx < -1 || planetIdx >= planets.getRewardsForPlanets().size()){
            MessageSenderService.sendMessage("PlanetIdxNotValid", sender);
            MessageSenderService.sendMessage(new AvailablePlanetsMessage(planets.getRewardsForPlanets(), planets.getPlanetsTaken()), sender);
            return;
        }

        if (planetIdx > -1 && planets.getPlanetsTaken()[planetIdx]) {
            MessageSenderService.sendMessage("PlanetAlreadyTaken", sender);
            MessageSenderService.sendMessage(new AvailablePlanetsMessage(planets.getRewardsForPlanets(), planets.getPlanetsTaken()), sender);
            return;
        }

        if (planetIdx == -1){
            // If he does not want to land
            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else {
            // If he wants to land
            planets.choosePlanet(player, planetIdx);

            gameManager.broadcastGameMessage(new AnotherPlayerLandedPlanetMessage(player, planetIdx));
            MessageSenderService.sendMessage("LandingCompleted", sender);

            rewardBoxes = planets.getRewardsForPlanets().get(planetIdx);
            phase = EventPhase.CHOOSE_BOX;
            MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
        }
    }

    /**
     * For each player receive the box that the player choose, and it's placement in the component
     * Update the player's view with the new list of available boxes
     *
     * @author Lorenzo
     * @param player that choose the box
     * @param rewardIdxBox chosen
     * @param yBoxStorage coordinate of the component were the box will be placed
     * @param xBoxStorage coordinate of the component were the box will be placed
     * @param idx is where the player want to insert the chosen box
     * @param sender current sender
     * @throws IllegalStateException
     */
    @Override
    public void receiveRewardBox(Player player, int rewardIdxBox, int xBoxStorage, int yBoxStorage, int idx, Sender sender) throws IllegalStateException {
        if (!phase.equals(EventPhase.CHOOSE_BOX)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks that current player is trying to get reward the reward box
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        if(rewardIdxBox == -1){
            MessageSenderService.sendMessage("PlanetLeft", sender);
            leavePlanet(player, sender);
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        if(xBoxStorage < 0 || yBoxStorage < 0 || xBoxStorage >= spaceshipMatrix[0].length || yBoxStorage >= spaceshipMatrix.length){
            MessageSenderService.sendMessage("InvalidCoordinates", sender);
            MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
            return;
        }

        Component boxStorage = spaceshipMatrix[yBoxStorage][xBoxStorage];

        if (boxStorage == null || !boxStorage.getType().equals(ComponentType.BOX_STORAGE) && !boxStorage.getType().equals(ComponentType.RED_BOX_STORAGE)) {
            MessageSenderService.sendMessage("InvalidCoordinates", sender);
            MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
            return;
        }

        // Checks that the box index is valid
        BoxStorage storage = (BoxStorage) boxStorage;
        if(idx >= storage.getCapacity() || idx < 0){
            MessageSenderService.sendMessage("InvalidStorageIndex", sender);
            MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
            return;
        }

        Box box = rewardBoxes.get(rewardIdxBox);

        // Checks that reward box is placed correctly in given storage
        try{
            planets.chooseRewardBox(player.getSpaceship(), (BoxStorage) boxStorage, box, idx);

            rewardBoxes.remove(box);
            gameManager.broadcastGameMessage(new BoxAddedMessage(player.getName(), xBoxStorage, yBoxStorage, rewardIdxBox, box));

        } catch (IllegalStateException e) {
            MessageSenderService.sendMessage(e.getMessage(), sender);
        }

        // All the boxes are chosen
        if (rewardBoxes.isEmpty()) {
            MessageSenderService.sendMessage("EmptyReward", sender);
            leavePlanet(player, sender);

        } else {
            MessageSenderService.sendMessage(new AvailableBoxesMessage(rewardBoxes), sender);
        }
    }

    /**
     * Function called if the player wants to leave
     *
     * @author Lorenzo
     * @param player current player
     * @param sender current sender
     * @throws IllegalStateException
     */
    private void leavePlanet(Player player, Sender sender) throws IllegalStateException {
        if (!phase.equals(EventPhase.CHOOSE_BOX))
            throw new IllegalStateException("IncorrectPhase");

        // Checks that current player is trying to leave planet
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYouTurn", sender);
            return;
        }

        player.setIsReady(true, gameManager.getGame());
        gameManager.getGameThread().notifyThread();
    }

    /**
     * Calculate the penalty for each landed player
     *
     * @author Lorenzo
     */
    private void eventEffect() {
        if (!phase.equals(EventPhase.EFFECT))
            throw new IllegalStateException("IncorrectPhase");

        for (Player player : planets.getLandedPlayers()){
            Sender sender = gameManager.getSenderByPlayer(player);

            MessageSenderService.sendMessage(new PlayerMovedBackwardMessage(planets.getPenaltyDays()), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedBackwardMessage(player.getName(), planets.getPenaltyDays()), sender);
        }

        // Penalty applied
        planets.penalty(gameManager.getGame().getBoard());
    }
}