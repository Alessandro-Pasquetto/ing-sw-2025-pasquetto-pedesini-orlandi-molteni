package org.progetto.server.controller.events;

import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.AffectedComponentMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.messages.toClient.Spaceship.UpdateOtherTravelersShipMessage;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.messages.toClient.Track.UpdateTrackMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.controller.GameController;
import org.progetto.server.controller.SpaceshipController;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.events.MeteorsRain;
import org.progetto.server.model.events.Projectile;
import org.progetto.server.model.events.ProjectileSize;

import java.util.ArrayList;

public class MeteorsRainController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private final MeteorsRain meteorsRain;
    private ArrayList<Player> activePlayers;
    private int diceResult;
    private Projectile comingMeteor;
    private final ArrayList<Player> decisionPlayers;
    private final ArrayList<Player> discardedBatteryPlayers;
    private final ArrayList<Player> handledPlayers;
    
    private void addDecisionPlayer(Player player) {
        synchronized (decisionPlayers) {
            decisionPlayers.add(player);
        }
    }

    private void addDiscardedBatteryPlayer(Player player) {
        synchronized (discardedBatteryPlayers) {
            discardedBatteryPlayers.add(player);
        }
    }

    public void addHandledPlayer(Player player) {
        synchronized (handledPlayers) {
            handledPlayers.add(player);
        }
    }
    
    private void removeDecisionPlayer(Player player) {
        synchronized (decisionPlayers) {
            decisionPlayers.remove(player);
        }
    }

    private void removeDiscardedBatteryPlayer(Player player) {
        synchronized (discardedBatteryPlayers) {
            discardedBatteryPlayers.remove(player);
        }
    }
    
    private void removeHandledPlayer(Player player) {
        synchronized (handledPlayers) {
            handledPlayers.remove(player);
        }
    }

    private boolean containsDiscardedBatteryPlayer(Player player) {
        synchronized (discardedBatteryPlayers) {
            return discardedBatteryPlayers.contains(player);
        }
    }

    private boolean containsDecisionPlayer(Player player){
        synchronized (decisionPlayers) {
            return decisionPlayers.contains(player);
        }
    }
    
    private boolean containsHandledPlayer(Player player){
        synchronized (handledPlayers) {
            return handledPlayers.contains(player);
        }
    }

    // =======================
    // CONSTRUCTORS
    // =======================

    public MeteorsRainController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.meteorsRain = (MeteorsRain) gameManager.getGame().getActiveEventCard();
        this.phase = EventPhase.START;
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();
        this.diceResult = 0;
        this.decisionPlayers = new ArrayList<>();
        this.discardedBatteryPlayers = new ArrayList<>();
        this.handledPlayers = new ArrayList<>();
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Starts event card effect
     *
     * @author Gabriele
     */
    @Override
    public void start() throws InterruptedException {
        if (!phase.equals(EventPhase.START))
            throw new IllegalStateException("IncorrectPhase");

        gameManager.broadcastGameMessage("ResetActivePlayer");

        phase = EventPhase.SEND_METEOR;
        sendMeteor();
    }

    @Override
    public boolean isParticipant(Player player){
        return activePlayers.contains(player);
    }

    /**
     * Send broadcast the incoming meteor information
     *
     * @author Gabriele
     */
    private void sendMeteor() throws InterruptedException {
        if (!phase.equals(EventPhase.SEND_METEOR))
            throw new IllegalStateException("IncorrectPhase");

        ArrayList<Projectile> meteors = meteorsRain.getMeteors();
        activePlayers = gameManager.getGame().getBoard().getCopyTravelers();

        for (Projectile meteor : meteors) {
            handledPlayers.clear();
            decisionPlayers.clear();
            discardedBatteryPlayers.clear();

            comingMeteor = meteor;

            // Sends to each player information about incoming meteor
            for (Player player : activePlayers) {
                Sender sender = gameManager.getSenderByPlayer(player);

                MessageSenderService.sendMessage(new IncomingProjectileMessage(comingMeteor), sender);
            }

            phase = EventPhase.ASK_ROLL_DICE;
            askToRollDice();

            // Delay to show the dice result
            gameManager.getGameThread().sleep(3000);

            handleMeteor();

            gameManager.getGameThread().sleep(3000);
        }
    }

    /**
     * Asks the leader to trow the dices
     *
     * @author Gabriele
     */
    private void askToRollDice() throws InterruptedException {
        if (!phase.equals(EventPhase.ASK_ROLL_DICE))
            throw new IllegalStateException("IncorrectPhase");

        Player leaderPlayer = activePlayers.getFirst();

        gameManager.getGame().setActivePlayer(leaderPlayer);

        Sender sender = gameManager.getSenderByPlayer(leaderPlayer);

        phase = EventPhase.ROLL_DICE;

        if (comingMeteor.getFrom() == 0 || comingMeteor.getFrom() == 2) {
            MessageSenderService.sendMessage("RollDiceToFindColumn", sender);
        }
        else if (comingMeteor.getFrom() == 1 || comingMeteor.getFrom() == 3) {
            MessageSenderService.sendMessage("RollDiceToFindRow", sender);
        }

        gameManager.getGameThread().resetAndWaitTravelerReady(leaderPlayer);

        // If the player is disconnected
        if(!leaderPlayer.getIsReady()){
            rollDice(leaderPlayer, null);
        }

        // Reset activePlayer
        gameManager.getGame().setActivePlayer(null);
    }

    /**
     * Rolls dice and collects the result
     *
     * @author Gabriele
     * @param player is the one that needs to trow the dices
     * @param sender current sender
     */
    @Override
    public void rollDice(Player player, Sender sender) {
        if (!phase.equals(EventPhase.ROLL_DICE)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls method is the leader
        if (!player.equals(activePlayers.getFirst())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        diceResult = player.rollDice();

        MessageSenderService.sendMessage(new DiceResultMessage(diceResult), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiceResultMessage(activePlayers.getFirst().getName(), diceResult), sender);

        player.setIsReady(true, gameManager.getGame());
        gameManager.getGameThread().notifyThread();
    }

    /**
     * Handles the meteor event
     *
     * @author Alessandro
     * @throws InterruptedException if the thread is interrupted
     */
    private void handleMeteor() throws InterruptedException{
        gameManager.getGameThread().resetTravelersReady();

        if (comingMeteor.getSize().equals(ProjectileSize.SMALL)) {
            phase = EventPhase.ASK_SMALL_METEOR_DECISION;
            askSmallMeteorDecision();
        } else {
            phase = EventPhase.ASK_BIG_METEOR_DECISION;
            askBigMeteorDecision();
        }

        gameManager.getGameThread().waitConnectedTravelersReady();

        // Handle disconnected travelers
        ArrayList<Player> disconnectedTravelers = GameController.getDisconnectedTravelers(gameManager);

        for (Player player : disconnectedTravelers) {
            if(containsHandledPlayer(player)){
                if(!player.getSpaceship().getBuildingBoard().checkShipValidityAndFixAliens()){
                    gameManager.getGame().getBoard().leaveTravel(player);
                    gameManager.broadcastGameMessage(new UpdateOtherTravelersShipMessage(gameManager.getGame().getBoard().getCopyTravelers()));
                    gameManager.broadcastGameMessage(new UpdateTrackMessage(GameController.getAllPlayersInTrackCopy(gameManager), gameManager.getGame().getBoard().getTrack()));
                }
            }else{
                handleCurrentMeteorForDisconnectedPlayer(player);
            }
        }
    }

    /**
     * Handles current small meteor for each player
     *
     * @author Gabriele
     */
    private void askSmallMeteorDecision() {
        if (!phase.equals(EventPhase.ASK_SMALL_METEOR_DECISION))
            throw new IllegalStateException("IncorrectPhase");

        // For each player checks impact point
        for (Player player : activePlayers) {

            // Retrieves current player sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            askSmallMeteorDecisionSinglePlayer(player, sender);
        }
    }

    /**
     * Handles current small meteor for a single player
     *
     * @author Alessandro
     * @param player is the one that needs to handle the current meteor
     * @param sender current sender
     */
    private void askSmallMeteorDecisionSinglePlayer(Player player, Sender sender) {
        // Finds impact component
        Component affectedComponent = meteorsRain.checkImpactComponent(gameManager.getGame(), player, comingMeteor, diceResult);

        // Checks if there is any affected component
        if (affectedComponent == null) {
            MessageSenderService.sendMessage("NoComponentHit", sender);
            addHandledPlayer(player);

            player.setIsReady(true, gameManager.getGame());
            return;
        }

        // Checks if component have any exposed connector in shot's direction
        if (affectedComponent.getConnections()[comingMeteor.getFrom()] == 0) {
            MessageSenderService.sendMessage("NoComponentDamaged", sender);
            addHandledPlayer(player);

            player.setIsReady(true, gameManager.getGame());
            return;
        }

        MessageSenderService.sendMessage(new AffectedComponentMessage(affectedComponent.getX(), affectedComponent.getY()), sender);

        // Checks for shields in that direction and at least a battery
        if (meteorsRain.checkShields(player, comingMeteor) && player.getSpaceship().getBatteriesCount() > 0) {
            addDecisionPlayer(player);
            MessageSenderService.sendMessage("AskToUseShield", sender);

        // Notifies component destruction
        } else {
            MessageSenderService.sendMessage("NoShieldAvailable", sender);
            handleCurrentMeteor(player);
        }
    }

    /**
     * Handles current big meteor for each player
     *
     * @author Gabriele
     */
    private void askBigMeteorDecision() {
        if (!phase.equals(EventPhase.ASK_BIG_METEOR_DECISION))
            throw new IllegalStateException("IncorrectPhase");

        // For each player checks impact point
        for (Player player : activePlayers) {

            // Retrieves current player sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            askBigMeteorDecisionSinglePlayer(player, sender);
        }
    }

    /**
     * Handles current big meteor for a single player
     *
     * @author Alessandro
     */
    private void askBigMeteorDecisionSinglePlayer(Player player, Sender sender) {
        // Finds impact component
        Component affectedComponent = meteorsRain.checkImpactComponent(gameManager.getGame(), player, comingMeteor, diceResult);

        // Checks if there is any affected component
        if (affectedComponent == null) {
            MessageSenderService.sendMessage("NoComponentHit", sender);
            addHandledPlayer(player);

            player.setIsReady(true, gameManager.getGame());
            return;
        }

        // Checks if component is a cannon positioned in the same direction as the meteor
        if (affectedComponent.getType().equals(ComponentType.CANNON) && affectedComponent.getRotation() == comingMeteor.getFrom()) {
            MessageSenderService.sendMessage("MeteorDestroyed", sender);
            addHandledPlayer(player);

            player.setIsReady(true, gameManager.getGame());
            return;
        }

        MessageSenderService.sendMessage(new AffectedComponentMessage(affectedComponent.getX(), affectedComponent.getY()), sender);

        // Checks if component is a double cannon positioned in the same direction as the meteor, and at least a battery
        if (affectedComponent.getType().equals(ComponentType.DOUBLE_CANNON) && affectedComponent.getRotation() == comingMeteor.getFrom() && player.getSpaceship().getBatteriesCount() > 0) {
            addDecisionPlayer(player);
            MessageSenderService.sendMessage("AskToUseDoubleCannon", sender);

        } else {
            MessageSenderService.sendMessage("NoCannonAvailable", sender);
            handleCurrentMeteor(player);
        }
    }

    /**
     * Receives player decision about the usage of shields or double cannon
     *
     * @author Gabriele
     * @param player is the one that send the decision
     * @param response is the given decision
     * @param sender current sender
     */
    @Override
    public void receiveProtectionDecision(Player player, String response, Sender sender) {

        // Checks if it is not part of non-protected player, and it is not already contained in protected one list
        if (!containsDecisionPlayer(player)) {
            MessageSenderService.sendMessage("CannotDecide", sender);
            return;
        }

        String upperCaseResponse = response.toUpperCase();

        switch (upperCaseResponse) {
            case "YES":
                addDiscardedBatteryPlayer(player);
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(1), sender);
                break;

            case "NO":
                removeDecisionPlayer(player);
                handleCurrentMeteor(player);
                break;

            default:
                MessageSenderService.sendMessage("IncorrectResponse", sender);
                break;
        }
    }

    /**
     * Receives the coordinates of BatteryStorage component from which remove a battery to activate protection
     *
     * @author Gabriele
     * @param player that send the decision about the battery position
     * @param xBatteryStorage coordinate of the storage
     * @param yBatteryStorage coordinate of the storage
     * @param sender current sender
     */
    @Override
    public void receiveDiscardedBatteries(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) {

        // Checks if the player that calls the methods has to discard a battery to activate protection
        if (!containsDiscardedBatteryPlayer(player)) {
            MessageSenderService.sendMessage("CannotDiscardBatteries", sender);
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xBatteryStorage < 0 || yBatteryStorage < 0 || yBatteryStorage >= spaceshipMatrix.length || xBatteryStorage >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendMessage("InvalidCoordinates", sender);
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(1), sender);
            return;
        }

        Component batteryStorageComp = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

        // Checks if component is a battery storage
        if (batteryStorageComp == null || !batteryStorageComp.getType().equals(ComponentType.BATTERY_STORAGE)) {
            MessageSenderService.sendMessage("InvalidComponent", sender);
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(1), sender);
            return;
        }

        BatteryStorage batteryStorage = (BatteryStorage) batteryStorageComp;

        if(batteryStorage.getItemsCount() == 0) {
            MessageSenderService.sendMessage("EmptyBatteryStorage", sender);
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(1), sender);
            return;
        }

        removeDiscardedBatteryPlayer(player);
        batteryStorage.decrementItemsCount(player.getSpaceship(), 1);

        MessageSenderService.sendMessage(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

        // Update spaceship to remove highlight components.
        MessageSenderService.sendMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player), sender);

        MessageSenderService.sendMessage("YouAreSafe", sender);

        player.setIsReady(true, gameManager.getGame());
        gameManager.getGameThread().notifyThread();
    }

    /**
     * Function called to handle current shot
     *
     * @author Gabriele
     */
    private void handleCurrentMeteor(Player player) {
        Game game = gameManager.getGame();
        // Gets current player sender reference
        Sender sender = gameManager.getSenderByPlayer(player);

        addHandledPlayer(player);

        // Handles current meteor for player
        Component affectedComponent = meteorsRain.checkImpactComponent(game, player, comingMeteor, diceResult);

        if(affectedComponent == null){
            MessageSenderService.sendMessage("NothingGotDestroyed", sender);
            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
            return;
        }

        // Destroys affected component
        if (SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, affectedComponent.getX(), affectedComponent.getY(), sender)){
            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else {
            MessageSenderService.sendMessage("AskSelectSpaceshipPart", sender);
        }
    }

    /**
     * Handles current meteor for disconnected players
     *
     * @author Alessandro
     * @param player is the one that needs to handle the current meteor
     */
    private void handleCurrentMeteorForDisconnectedPlayer(Player player) {
        Game game = gameManager.getGame();

        // Handles current meteor for player
        Component affectedComponent = meteorsRain.checkImpactComponent(game, player, comingMeteor, diceResult);

        if(affectedComponent == null)
            return;

        // Gets current player sender reference
        Sender sender = gameManager.getSenderByPlayer(player);

        // Destroys affected component
        if(!SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, affectedComponent.getX(), affectedComponent.getY(), sender)){
            gameManager.getGame().getBoard().leaveTravel(player);
            gameManager.broadcastGameMessage(new UpdateOtherTravelersShipMessage(gameManager.getGame().getBoard().getCopyTravelers()));
            gameManager.broadcastGameMessage(new UpdateTrackMessage(GameController.getAllPlayersInTrackCopy(gameManager), gameManager.getGame().getBoard().getTrack()));
        }
    }

    /**
     * Reconnects player to the event
     *
     * @author Alessandro
     * @param player is the one that needs to be reconnected
     * @param sender current sender
     */
    @Override
    public void reconnectPlayer(Player player, Sender sender){
        if(!activePlayers.contains(player))
            return;

        if(phase.equals(EventPhase.ROLL_DICE) && player.equals(gameManager.getGame().getActivePlayer())){
            MessageSenderService.sendMessage(new IncomingProjectileMessage(comingMeteor), sender);

            if (comingMeteor.getFrom() == 0 || comingMeteor.getFrom() == 2) {
                MessageSenderService.sendMessage("RollDiceToFindColumn", sender);
            }
            else if (comingMeteor.getFrom() == 1 || comingMeteor.getFrom() == 3) {
                MessageSenderService.sendMessage("RollDiceToFindRow", sender);
            }

            return;
        }

        player.setIsReady(false, gameManager.getGame());

        // Reset decisions
        removeDecisionPlayer(player);
        removeDiscardedBatteryPlayer(player);

        if(containsHandledPlayer(player)){
            if(player.getSpaceship().getBuildingBoard().checkShipValidityAndFixAliens()){
                MessageSenderService.sendMessage("MeteorAlreadyHandled", sender);
                player.setIsReady(true, gameManager.getGame());
            }
            else
                MessageSenderService.sendMessage("AskSelectSpaceshipPart", sender);

            return;
        }

        MessageSenderService.sendMessage(new IncomingProjectileMessage(comingMeteor), sender);

        if(comingMeteor.getSize().equals(ProjectileSize.SMALL))
            askSmallMeteorDecisionSinglePlayer(player, sender);
        else
            askBigMeteorDecisionSinglePlayer(player, sender);
    }
}