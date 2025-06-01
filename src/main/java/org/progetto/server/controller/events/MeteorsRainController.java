package org.progetto.server.controller.events;

import org.progetto.messages.toClient.AffectedComponentMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.messages.toClient.Spaceship.UpdateOtherTravelersShipMessage;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
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
    private final ArrayList<Player> discardedBattery;
    private final ArrayList<Player> meteoriteHandlers;

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
        this.discardedBattery = new ArrayList<>();
        this.meteoriteHandlers = new ArrayList<>();
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
            meteoriteHandlers.clear();
            comingMeteor = meteor;

            // Sends to each player information about incoming meteor
            for (Player player : activePlayers) {
                Sender sender = gameManager.getSenderByPlayer(player);

                MessageSenderService.sendOptional(new IncomingProjectileMessage(meteor), sender);
            }

            phase = EventPhase.ASK_ROLL_DICE;
            askToRollDice();

            // Delay to show the dice result
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " was interrupted during sleep.");
                e.printStackTrace();
            }

            handleMeteor();

            // Resets elaboration attributes
            decisionPlayers.clear();
            discardedBattery.clear();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " was interrupted during sleep.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Asks the leader to trow the dices
     *
     * @author Gabriele
     */
    private void askToRollDice() {
        if (!phase.equals(EventPhase.ASK_ROLL_DICE))
            throw new IllegalStateException("IncorrectPhase");

        Player activePlayer = activePlayers.getFirst();

        Sender sender = gameManager.getSenderByPlayer(activePlayer);

        phase = EventPhase.ROLL_DICE;
        try{
            if (comingMeteor.getFrom() == 0 || comingMeteor.getFrom() == 2) {
                MessageSenderService.sendCritical("RollDiceToFindColumn", sender);
            }

            else if (comingMeteor.getFrom() == 1 || comingMeteor.getFrom() == 3) {
                MessageSenderService.sendCritical("RollDiceToFindRow", sender);
            }

            gameManager.getGameThread().resetAndWaitTravelerReady(activePlayer);

            // If the player is disconnected
            if(!activePlayer.getIsReady()){
                rollDice(activePlayer, sender);
            }

        } catch (Exception e) {
            rollDice(activePlayer, sender);
        }
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls method is the leader
        if (!player.equals(activePlayers.getFirst())) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
            return;
        }

        diceResult = player.rollDice();

        MessageSenderService.sendOptional(new DiceResultMessage(diceResult), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiceResultMessage(activePlayers.getFirst().getName(), diceResult), sender);

        player.setIsReady(true, gameManager.getGame());
        gameManager.getGameThread().notifyThread();
    }

    private void handleMeteor() throws InterruptedException{
        gameManager.getGameThread().resetTravelersReady();

        if (comingMeteor.getSize().equals(ProjectileSize.SMALL)) {
            phase = EventPhase.HANDLE_SMALL_METEOR;
            handleSmallMeteor();
        } else {
            phase = EventPhase.HANDLE_BIG_METEOR;
            handleBigMeteor();
        }

        gameManager.getGameThread().waitConnectedTravelersReady();

        // Handle disconnected travelers
        ArrayList<Player> disconnectedTravelers = GameController.getDisconnectedTravelers(gameManager);

        for (Player player : disconnectedTravelers) {
            if(meteoriteHandlers.contains(player)){
                if(!player.getSpaceship().getBuildingBoard().checkShipValidityAndFixAliens()){
                    gameManager.getGame().getBoard().leaveTravel(player);
                    gameManager.broadcastGameMessage(new UpdateOtherTravelersShipMessage(gameManager.getGame().getBoard().getCopyTravelers()));
                }
            }else{
                handleCurrentMeteorForDisconnected(player);
            }
        }
    }

    /**
     * Handles current small meteor for each player
     *
     * @author Gabriele
     */
    private void handleSmallMeteor() {
        if (!phase.equals(EventPhase.HANDLE_SMALL_METEOR))
            throw new IllegalStateException("IncorrectPhase");

        // For each player checks impact point
        for (Player player : activePlayers) {

            // Retrieves current player sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            // Finds impact component
            Component affectedComponent = meteorsRain.checkImpactComponent(gameManager.getGame(), player, comingMeteor, diceResult);

            // Checks if there is any affected component
            if (affectedComponent == null) {
                MessageSenderService.sendOptional("NoComponentHit", sender);
                meteoriteHandlers.add(player);

                player.setIsReady(true, gameManager.getGame());
                continue;
            }

            // Checks if component have any exposed connector in shot's direction
            if (affectedComponent.getConnections()[comingMeteor.getFrom()] == 0) {
                MessageSenderService.sendOptional("NoComponentDamaged", sender);
                meteoriteHandlers.add(player);

                player.setIsReady(true, gameManager.getGame());
                continue;
            }

            // Checks for shields in that direction and at least a battery
            if (meteorsRain.checkShields(player, comingMeteor) && player.getSpaceship().getBatteriesCount() > 0) {
                decisionPlayers.add(player);
                MessageSenderService.sendOptional(new AffectedComponentMessage(affectedComponent.getX(), affectedComponent.getY()), sender);

            // Notifies component destruction
            } else {
                MessageSenderService.sendOptional("NoShieldAvailable", sender);
                handleCurrentMeteor(player);
            }
        }

        // Checks if there is at least one player that can decide to use a shield
        if (!decisionPlayers.isEmpty()) {
            phase = EventPhase.ASK_TO_PROTECT;
            askToProtect();
        }
    }

    /**
     * Handles current small meteor for each player
     *
     * @author Gabriele
     */
    private void handleBigMeteor() {
        if (!phase.equals(EventPhase.HANDLE_BIG_METEOR))
            throw new IllegalStateException("IncorrectPhase");

        // For each player checks impact point
        for (Player player : activePlayers) {

            // Retrieves current player sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            // Finds impact component
            Component affectedComponent = meteorsRain.checkImpactComponent(gameManager.getGame(), player, comingMeteor, diceResult);

            // Checks if there is any affected component
            if (affectedComponent == null) {
                MessageSenderService.sendOptional("NoComponentHit", sender);
                meteoriteHandlers.add(player);

                player.setIsReady(true, gameManager.getGame());
                continue;
            }

            // Checks if component is a cannon positioned in the same direction as the meteor
            if (affectedComponent.getType().equals(ComponentType.CANNON) && affectedComponent.getRotation() == comingMeteor.getFrom()) {
                MessageSenderService.sendOptional("MeteorDestroyed", sender);
                meteoriteHandlers.add(player);

                player.setIsReady(true, gameManager.getGame());
                continue;
            }

            // Checks if component is a double cannon positioned in the same direction as the meteor, and at least a battery
            if (affectedComponent.getType().equals(ComponentType.DOUBLE_CANNON) && affectedComponent.getRotation() == comingMeteor.getFrom() && player.getSpaceship().getBatteriesCount() > 0) {
                decisionPlayers.add(player);
                MessageSenderService.sendOptional(new AffectedComponentMessage(affectedComponent.getX(), affectedComponent.getY()), sender);

            // Notifies component destruction
            } else {
                MessageSenderService.sendOptional("NoCannonAvailable", sender);
                handleCurrentMeteor(player);
            }
        }

        // Checks if there is at least one player that can decide to use a double cannon
        if (!decisionPlayers.isEmpty()) {
            phase = EventPhase.ASK_TO_PROTECT;
            askToProtect();
        }
    }

    /**
     * Asks players if they want to protect (shield or double cannon)
     *
     * @author Gabriele
     */
    private void askToProtect() {
        if (!phase.equals(EventPhase.ASK_TO_PROTECT))
            throw new IllegalStateException("IncorrectPhase");

        for (Player player : decisionPlayers) {
            askToProtectToSinglePlayer(player);
        }
    }

    private void askToProtectToSinglePlayer(Player player) {
        // Asks current player if he wants to use a protection
        Sender sender = gameManager.getSenderByPlayer(player);

        if (comingMeteor.getSize().equals(ProjectileSize.SMALL))
            MessageSenderService.sendOptional("AskToUseShield", sender);
        else
            MessageSenderService.sendOptional("AskToUseDoubleCannon", sender);
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
    public synchronized void receiveProtectionDecision(Player player, String response, Sender sender) {

        // Checks if it is not part of non-protected player, and it is not already contained in protected one list
        if (!decisionPlayers.contains(player)) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
            return;
        }

        String upperCaseResponse = response.toUpperCase();

        switch (upperCaseResponse) {
            case "YES":
                discardedBattery.add(player);
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(1), sender);
                break;

            case "NO":
                handleCurrentMeteor(player);
                break;

            default:
                MessageSenderService.sendOptional("IncorrectResponse", sender);
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
    public synchronized void receiveDiscardedBatteries(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) {

        // Checks if the player that calls the methods has to discard a battery to activate protection
        if (!discardedBattery.contains(player)) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xBatteryStorage < 0 || yBatteryStorage < 0 || yBatteryStorage >= spaceshipMatrix.length || xBatteryStorage >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendOptional("InvalidCoordinates", sender);
            MessageSenderService.sendOptional(new BatteriesToDiscardMessage(1), sender);
            return;
        }

        Component batteryStorageComp = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

        // Checks if component is a battery storage
        if (batteryStorageComp == null || !batteryStorageComp.getType().equals(ComponentType.BATTERY_STORAGE)) {
            MessageSenderService.sendOptional("InvalidComponent", sender);
            MessageSenderService.sendOptional(new BatteriesToDiscardMessage(1), sender);
            return;
        }

        BatteryStorage batteryStorage = (BatteryStorage) batteryStorageComp;

        if(batteryStorage.getItemsCount() == 0) {
            MessageSenderService.sendOptional("EmptyBatteryStorage", sender);
            MessageSenderService.sendOptional(new BatteriesToDiscardMessage(1), sender);
            return;
        }

        discardedBattery.remove(player);
        batteryStorage.decrementItemsCount(player.getSpaceship(), 1);

        MessageSenderService.sendOptional(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

        // Update spaceship to remove highlight components when it's not my turn.
        // For others, it's used to reload the spaceship in case they got disconnected while it was discarding.
        gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));

        MessageSenderService.sendOptional("YouAreSafe", sender);

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

        // Handles current meteor for player
        Component affectedComponent = meteorsRain.checkImpactComponent(game, player, comingMeteor, diceResult);

        // Gets current player sender reference
        Sender sender = gameManager.getSenderByPlayer(player);

        // Destroys affected component
        if (SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, affectedComponent.getX(), affectedComponent.getY(), sender)){
            meteoriteHandlers.add(player);
            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else {
            meteoriteHandlers.add(player);
            MessageSenderService.sendOptional("AskSelectSpaceshipPart", sender);
        }
    }

    /**
     * Handles current meteor for disconnected players
     *
     * @author Alessandro
     * @param player is the one that needs to handle the current meteor
     */
    private void handleCurrentMeteorForDisconnected(Player player) {
        Game game = gameManager.getGame();

        // Handles current meteor for player
        Component affectedComponent = meteorsRain.checkImpactComponent(game, player, comingMeteor, diceResult);

        // Gets current player sender reference
        Sender sender = gameManager.getSenderByPlayer(player);

        // Destroys affected component
        if(!SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, affectedComponent.getX(), affectedComponent.getY(), sender)){
            gameManager.getGame().getBoard().leaveTravel(player);
            gameManager.broadcastGameMessage(new UpdateOtherTravelersShipMessage(gameManager.getGame().getBoard().getCopyTravelers()));
        }
    }

    @Override
    public void reconnectPlayer(Player player, Sender sender){
        if(!activePlayers.contains(player))
            return;

        if(meteoriteHandlers.contains(player)){
            MessageSenderService.sendOptional("MeteorAlreadyHandled", sender);
            return;
        }

        Component affectedComponent = meteorsRain.checkImpactComponent(gameManager.getGame(), player, comingMeteor, diceResult);

        // Checks if component is a double cannon positioned in the same direction as the meteor, and at least a battery
        if (affectedComponent.getType().equals(ComponentType.DOUBLE_CANNON) && affectedComponent.getRotation() == comingMeteor.getFrom() && player.getSpaceship().getBatteriesCount() > 0) {
            MessageSenderService.sendOptional(new AffectedComponentMessage(affectedComponent.getX(), affectedComponent.getY()), sender);
            askToProtectToSinglePlayer(player);

        } else {
            MessageSenderService.sendOptional("NoCannonAvailable", sender);
            handleCurrentMeteor(player);
        }
    }
}