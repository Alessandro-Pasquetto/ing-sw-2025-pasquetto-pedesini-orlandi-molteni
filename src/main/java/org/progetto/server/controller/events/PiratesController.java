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
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.events.Pirates;
import org.progetto.server.model.events.Projectile;
import org.progetto.server.model.events.ProjectileSize;

import java.util.ArrayList;

public class PiratesController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Pirates pirates;
    private Player leaderPlayer;
    private final ArrayList<Player> activePlayers;
    private float playerFirePower;
    private int requestedBatteries;
    private int diceResult;
    private boolean defeated;
    private final ArrayList<Projectile> penaltyShots;
    private Projectile currentShot;
    private final ArrayList<Player> defeatedPlayers;

    private final ArrayList<BatteryStorage> batteryStorages;
    private final ArrayList<Player> shieldPlayers;
    private final ArrayList<Player> shieldBatteryPlayers;
    private final ArrayList<Player> handledPlayers;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PiratesController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.pirates = (Pirates) gameManager.getGame().getActiveEventCard();
        this.phase = EventPhase.START;
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();
        this.playerFirePower = 0;
        this.requestedBatteries = 0;
        this.penaltyShots = new ArrayList<>(pirates.getPenaltyShots());
        this.currentShot = null;
        this.diceResult = 0;
        this.defeated = false;
        this.defeatedPlayers = new ArrayList<>();

        this.batteryStorages = new ArrayList<>();
        this.shieldPlayers = new ArrayList<>();
        this.shieldBatteryPlayers = new ArrayList<>();
        this.handledPlayers = new ArrayList<>();
    }

    private void addDefeatedPlayer(Player player) {
        synchronized (defeatedPlayers) {
            defeatedPlayers.add(player);
        }
    }

    private void addShieldPlayer(Player player) {
        synchronized (shieldPlayers) {
            shieldPlayers.add(player);
        }
    }

    private void addShieldBatteryPlayer(Player player) {
        synchronized (shieldBatteryPlayers) {
            shieldBatteryPlayers.add(player);
        }
    }

    public void addHandledPlayer(Player player) {
        synchronized (handledPlayers) {
            handledPlayers.add(player);
        }
    }

    private void removeDefeatedPlayer(Player player) {
        synchronized (defeatedPlayers) {
            defeatedPlayers.remove(player);
        }
    }

    private void removeShieldPlayer(Player player) {
        synchronized (shieldPlayers) {
            shieldPlayers.remove(player);
        }
    }

    private void removeShieldBatteryPlayer(Player player) {
        synchronized (shieldBatteryPlayers) {
            shieldBatteryPlayers.remove(player);
        }
    }

    private void removeHandledPlayer(Player player) {
        synchronized (handledPlayers) {
            handledPlayers.remove(player);
        }
    }

    private boolean containsDefeatedPlayer(Player player) {
        synchronized (defeatedPlayers) {
            return defeatedPlayers.contains(player);
        }
    }

    private boolean containsShieldPlayer(Player player) {
        synchronized (shieldPlayers) {
            return shieldPlayers.contains(player);
        }
    }

    private boolean containsShieldBatteryPlayer(Player player) {
        synchronized (shieldBatteryPlayers) {
            return shieldBatteryPlayers.contains(player);
        }
    }

    private boolean containsHandledPlayer(Player player) {
        synchronized (handledPlayers) {
            return handledPlayers.contains(player);
        }
    }
    
    private Player getFirstDefeatedPlayer() {
        synchronized (defeatedPlayers) {
            return defeatedPlayers.getFirst();
        }
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Starts event card effect
     *
     * @author Gabriele
     * @throws InterruptedException
     */
    @Override
    public void start() throws InterruptedException {
        if (phase.equals(EventPhase.START)) {
            phase = EventPhase.ASK_CANNONS;
            askHowManyCannonsToUse();
        }
    }

    /**
     * Asks current player how many double cannons he wants to use
     *
     * @author Gabriele
     * @throws InterruptedException
     */
    private void askHowManyCannonsToUse() throws InterruptedException {
        if (!phase.equals(EventPhase.ASK_CANNONS))
            throw new IllegalStateException("IncorrectPhase");

        for (Player player : activePlayers) {

            // Checks if card got defeated
            if (defeated) {
                gameManager.broadcastGameMessage("RaidersDefeated");
                break;
            }

            gameManager.getGame().setActivePlayer(player);
            gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            Spaceship spaceship = player.getSpaceship();

            // Calculates max number of double cannons usable
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

            // If he can use any double cannon, and he doesn't win with normalShootingPower
            if (maxUsable != 0 && pirates.battleResult(playerFirePower) != 1) {
                batteryStorages.clear();

                phase = EventPhase.CANNON_NUMBER;
                MessageSenderService.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, pirates.getFirePowerRequired(), player.getSpaceship().getNormalShootingPower()), sender);

                gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                gameManager.getGameThread().resetAndWaitTravelerReady(player);

                // Update spaceship to remove highlight components
                // For others, it's used to reload the spaceship in case of disconnections while he was discarding.
                gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));

                // If the player is disconnected
                if(!player.getIsReady()){
                    playerFirePower = spaceship.getNormalShootingPower();
                }
            }

            phase = EventPhase.BATTLE_RESULT;
            battleResult(player, sender);
        }

        gameManager.getGameThread().sleep(3000);

        if(!defeatedPlayers.isEmpty()){
            phase = EventPhase.HANDLE_DEFEATED_PLAYERS;
            handleDefeatedPlayers();
        }
    }

    /**
     * Receives numbers of double cannons to use
     *
     * @author Gabriele
     * @param player current player
     * @param num number of cannons player want to use
     * @param sender current sender
     */
    @Override
    public void receiveHowManyCannonsToUse(Player player, int num, Sender sender) {
        if (!phase.equals(EventPhase.CANNON_NUMBER)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        Spaceship spaceship = player.getSpaceship();

        // Player doesn't want to use double cannons
        if (num == 0) {
            playerFirePower = player.getSpaceship().getNormalShootingPower();
            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else if (num <= (spaceship.getFullDoubleCannonCount() + spaceship.getHalfDoubleCannonCount()) && num <= spaceship.getBatteriesCount() && num > 0) {
            requestedBatteries = num;

            // Updates player's firepower based on his decision
            if (num <= spaceship.getFullDoubleCannonCount()) {
                playerFirePower = spaceship.getNormalShootingPower() + 2 * num;
            } else {
                playerFirePower = spaceship.getNormalShootingPower() + 2 * spaceship.getFullDoubleCannonCount() + (num - spaceship.getFullDoubleCannonCount());
            }

            phase = EventPhase.DISCARDED_BATTERIES;
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(num), sender);

        } else {
            MessageSenderService.sendMessage("IncorrectNumber", sender);
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();
            MessageSenderService.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, pirates.getFirePowerRequired(), player.getSpaceship().getNormalShootingPower()), sender);
        }
    }

    /**
     * Receives the coordinates of BatteryStorage component from which remove a battery
     *
     * @author Gabriele
     * @param player current player
     * @param xBatteryStorage x coordinate of chosen battery storage
     * @param yBatteryStorage y coordinate of chosen battery storage
     * @param sender current sender
     */
    @Override
    public void receiveDiscardedBatteries(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) {

        boolean isDiscardingShieldBattery = false;

        if(phase.equals(EventPhase.DISCARDED_BATTERIES)){
            if (!player.equals(gameManager.getGame().getActivePlayer())) {
                MessageSenderService.sendMessage("NotYourTurn", sender);
                return;
            }
        }
        else if(shieldPlayers.contains(player)){
            isDiscardingShieldBattery = true;
        }
        else{
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xBatteryStorage < 0 || yBatteryStorage < 0 || yBatteryStorage >= spaceshipMatrix.length || xBatteryStorage >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendMessage("InvalidCoordinates", sender);

            if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

            } else if (isDiscardingShieldBattery) {
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(1), sender);
            }
            return;
        }

        Component batteryStorageComp = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

        // Checks if component is a battery storage
        if (batteryStorageComp == null || !batteryStorageComp.getType().equals(ComponentType.BATTERY_STORAGE)) {
            MessageSenderService.sendMessage("InvalidComponent", sender);

            if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

            } else if (isDiscardingShieldBattery) {
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(1), sender);
            }
            return;
        }

        BatteryStorage batteryStorage = (BatteryStorage) batteryStorageComp;

        if(batteryStorage.getItemsCount() == 0) {
            MessageSenderService.sendMessage("EmptyBatteryStorage", sender);

            if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

            } else if (isDiscardingShieldBattery) {
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(1), sender);
            }
            return;
        }

        if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {

            batteryStorages.add(batteryStorage);
            requestedBatteries--;

            MessageSenderService.sendMessage(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

            if (requestedBatteries == 0) {

                for (BatteryStorage component : batteryStorages) {
                    component.decrementItemsCount(player.getSpaceship(), 1);
                }

                player.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();

            } else {
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);
            }

        } else if (isDiscardingShieldBattery) {

            removeShieldPlayer(player);
            batteryStorage.decrementItemsCount(player.getSpaceship(), 1);

            MessageSenderService.sendMessage(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

            // Update spaceship to remove highlight components.
            MessageSenderService.sendMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player), sender);

            MessageSenderService.sendMessage("YouAreSafe", sender);

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
        }
    }

    /**
     * Check the result of the battle and goes to a new phase based on the result
     *
     * @author Gabriele
     * @param player current player
     * @param sender current sender
     */
    private void battleResult(Player player, Sender sender) throws InterruptedException {
        if (!phase.equals(EventPhase.BATTLE_RESULT))
            throw new IllegalStateException("IncorrectPhase");

        // Calls the battleResult function
        switch (pirates.battleResult(playerFirePower)) {
            case 1:
                MessageSenderService.sendMessage("YouWonBattle", sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerWonBattleMessage(player.getName()), sender);
                defeated = true;

                phase = EventPhase.REWARD_DECISION;
                MessageSenderService.sendMessage(new AcceptRewardCreditsAndPenaltyDaysMessage(pirates.getRewardCredits(), pirates.getPenaltyDays()), sender);

                gameManager.getGameThread().resetAndWaitTravelerReady(player);
                break;

            case 0:
                MessageSenderService.sendMessage("YouDrewBattle", sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerDrewBattleMessage(player.getName()), sender);
                break;

            case -1:
                addDefeatedPlayer(player);

                MessageSenderService.sendMessage("YouLostBattle", sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerLostBattleMessage(player.getName()), sender);
                break;
        }
    }

    /**
     * Receives response for rewardPenalty
     *
     * @author Gabriele
     * @param player current player
     * @param response player's response
     * @param sender current sender
     */
    public void receiveRewardDecision(Player player, String response, Sender sender) {
        if (!phase.equals(EventPhase.REWARD_DECISION)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if current player is active one
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        String upperCaseResponse = response.toUpperCase();

        switch (upperCaseResponse) {
            case "YES":
                phase = EventPhase.EFFECT;
                eventEffect();
                break;

            case "NO":
                player.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();
                break;

            default:
                MessageSenderService.sendMessage("IncorrectResponse", sender);
                MessageSenderService.sendMessage(new AcceptRewardCreditsAndPenaltyDaysMessage(pirates.getRewardCredits(), pirates.getPenaltyDays()), sender);
                break;
        }
    }

    /**
     * If the player accepted, he receives the reward and loses the penalty days
     *
     * @author Gabriele
     */
    private void eventEffect() {
        if (!phase.equals(EventPhase.EFFECT))
            throw new IllegalStateException("IncorrectPhase");

        Player player = gameManager.getGame().getActivePlayer();

        // Event effect applied for single player
        pirates.rewardPenalty(gameManager.getGame().getBoard(), player);

        // Retrieves sender reference
        Sender sender = gameManager.getSenderByPlayer(player);

        MessageSenderService.sendMessage(new PlayerMovedBackwardMessage(pirates.getPenaltyDays()), sender);
        MessageSenderService.sendMessage(new PlayerGetsCreditsMessage(pirates.getRewardCredits()), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedBackwardMessage(player.getName(), pirates.getPenaltyDays()), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerGetsCreditsMessage(player.getName(), pirates.getRewardCredits()), sender);

        player.setIsReady(true, gameManager.getGame());
        gameManager.getGameThread().notifyThread();
    }

    /**
     * Handles defeated players
     *
     * @author Gabriele
     */
    private void handleDefeatedPlayers() throws InterruptedException {
        if (!phase.equals(EventPhase.HANDLE_DEFEATED_PLAYERS))
            throw new IllegalStateException("IncorrectPhase");

        gameManager.broadcastGameMessage("ResetActivePlayer");

        ArrayList<Player> originalDefeatedPlayers = new ArrayList<>(defeatedPlayers);

        for (Projectile shot : penaltyShots) {
            // Resets elaboration attributes
            defeatedPlayers.clear();
            defeatedPlayers.addAll(originalDefeatedPlayers);

            currentShot = shot;

            // Sends to each defeated player information about incoming shot
            for (Player defeatedPlayer : defeatedPlayers) {
                Sender sender = gameManager.getSenderByPlayer(defeatedPlayer);

                MessageSenderService.sendMessage(new IncomingProjectileMessage(currentShot), sender);
            }

            phase = EventPhase.ASK_ROLL_DICE;
            askToRollDice();

            // Delay to show the dice result
            gameManager.getGameThread().sleep(3000);

            handleShotsDefeatedPlayers();

            gameManager.getGameThread().sleep(3000);
        }
    }

    /**
     * Asks first penalized player to roll dice
     *
     * @author Gabriele
     */
    private void askToRollDice() throws InterruptedException {
        if (!phase.equals(EventPhase.ASK_ROLL_DICE))
            throw new IllegalStateException("IncorrectPhase");

        leaderPlayer = getFirstDefeatedPlayer();

        // Asks first defeated player to roll dice
        Sender sender = gameManager.getSenderByPlayer(leaderPlayer);

        phase = EventPhase.ROLL_DICE;

        if (currentShot.getFrom() == 0 || currentShot.getFrom() == 2) {
            MessageSenderService.sendMessage("RollDiceToFindColumn", sender);

        } else if (currentShot.getFrom() == 1 || currentShot.getFrom() == 3) {
            MessageSenderService.sendMessage("RollDiceToFindRow", sender);
        }

        gameManager.getGameThread().resetAndWaitTravelerReady(leaderPlayer);

        // If the player is disconnected
        if(!leaderPlayer.getIsReady()){
            rollDice(leaderPlayer, sender);
        }
    }

    /**
     * Rolls dice and collects the result
     *
     * @author Gabriele
     * @param player current player
     * @param sender current sender
     */
    @Override
    public void rollDice(Player player, Sender sender) {
        if (!phase.equals(EventPhase.ROLL_DICE)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the first defeated player
        if (!player.equals(getFirstDefeatedPlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        diceResult = player.rollDice();

        MessageSenderService.sendMessage(new DiceResultMessage(diceResult), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiceResultMessage(player.getName(), diceResult), sender);

        player.setIsReady(true, gameManager.getGame());
        gameManager.getGameThread().notifyThread();
    }

    /**
     * Handles shots for defeated players
     *
     * @author Alessandro
     * @throws InterruptedException
     */
    private void handleShotsDefeatedPlayers() throws InterruptedException {
        // Reset defeatedPlayers
        for (Player defeatedPlayer : defeatedPlayers) {
            defeatedPlayer.setIsReady(false, gameManager.getGame());
        }

        // Checks projectile dimensions
        if (currentShot.getSize().equals(ProjectileSize.SMALL)) {
            phase = EventPhase.ASK_SHIELDS;
            askToUseShields();
        }
        else{
            for (Player defeatedPlayer : defeatedPlayers) {
                handleCurrentShot(defeatedPlayer);
            }
        }

        gameManager.getGameThread().waitParameterPlayersReady(defeatedPlayers);

        // Handle disconnected defeated players
        ArrayList<Player> disconnectedDefeatedPlayers = new ArrayList<>(defeatedPlayers
                .stream()
                .filter(player -> gameManager.getDisconnectedPlayersCopy().contains(player))
                .toList());


        for (Player player : disconnectedDefeatedPlayers) {
            if(containsHandledPlayer(player)){
                if(!player.getSpaceship().getBuildingBoard().checkShipValidityAndFixAliens()){
                    gameManager.getGame().getBoard().leaveTravel(player);
                    gameManager.broadcastGameMessage(new UpdateOtherTravelersShipMessage(gameManager.getGame().getBoard().getCopyTravelers()));
                    gameManager.broadcastGameMessage(new UpdateTrackMessage(GameController.getAllPlayersInTrackCopy(gameManager), gameManager.getGame().getBoard().getTrack()));
                }
            }else{
                handleCurrentShotForDisconnectedPlayer(player);
            }
        }
    }

    /**
     * Asks defeated players if they want to use shields to protect
     *
     * @author Gabriele
     */
    private void askToUseShields() {
        if (!phase.equals(EventPhase.ASK_SHIELDS))
            throw new IllegalStateException("IncorrectPhase");

        for (Player defeatedPlayer : defeatedPlayers) {
            askToUseShieldSinglePlayer(defeatedPlayer);
        }
    }

    /**
     * Asks current defeated player if he wants to use a shield
     *
     * @author Alessandro
     * @param defeatedPlayer current defeated player
     */
    private void askToUseShieldSinglePlayer(Player defeatedPlayer) {
        // Asks current defeated player if he wants to use a shield
        Sender sender = gameManager.getSenderByPlayer(defeatedPlayer);

        // Finds impact component
        Component affectedComponent = pirates.penaltyShot(gameManager.getGame(), defeatedPlayer, currentShot, diceResult);

        // Checks if there is any affected component
        if (affectedComponent == null) {
            MessageSenderService.sendMessage("NoComponentHit", sender);
            addHandledPlayer(defeatedPlayer);
            defeatedPlayer.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
            return;
        }

        MessageSenderService.sendMessage(new AffectedComponentMessage(affectedComponent.getX(), affectedComponent.getY()), sender);

        // Checks if current player has a shield that covers that direction
        boolean hasShield = pirates.checkShields(defeatedPlayer, currentShot);

        if (hasShield && defeatedPlayer.getSpaceship().getBatteriesCount() > 0) {
            addShieldPlayer(defeatedPlayer);
            MessageSenderService.sendMessage("AskToUseShield", sender);

        } else {
            MessageSenderService.sendMessage("NoShieldAvailable", sender);
            handleCurrentShot(defeatedPlayer);
        }
    }

    /**
     * Receives player's decision about the usage of a shield
     *
     * @author Gabriele
     * @param player current player
     * @param response player's response
     * @param sender current sender
     */
    @Override
    public void receiveProtectionDecision(Player player, String response, Sender sender) {

        // Checks if it is not part of non-protected player, and it is not already contained in protected one list
        if (!containsShieldPlayer(player)) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        String upperCaseResponse = response.toUpperCase();

        switch (upperCaseResponse) {
            case "YES":
                addShieldBatteryPlayer(player);
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(1), sender);
                break;

            case "NO":
                MessageSenderService.sendMessage("YouAnsweredNo", sender);
                handleCurrentShot(player);
                break;

            default:
                MessageSenderService.sendMessage("IncorrectResponse", sender);
                MessageSenderService.sendMessage("AskToUseShield", sender);
                break;
        }
    }

    /**
     * Handles current shot
     *
     * @author Gabriele
     */
    private void handleCurrentShot(Player player) {

        Game game = gameManager.getGame();
        Projectile shot = currentShot;

        // Gets current player sender reference
        Sender sender = gameManager.getSenderByPlayer(player);

        Component destroyedComponent = pirates.penaltyShot(game, player, shot, diceResult);

        handledPlayers.add(player);

        // Sends two types of messages based on the shot's result
        if (destroyedComponent == null) {
            MessageSenderService.sendMessage("NothingGotDestroyed", sender);
            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
            return;
        }

        if (SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, destroyedComponent.getX(), destroyedComponent.getY(), sender)){
            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else
            MessageSenderService.sendMessage("AskSelectSpaceshipPart", sender);
    }

    /**
     * Handles current shot for disconnected player
     *
     * @author Alessandro
     * @param player current player
     */
    private void handleCurrentShotForDisconnectedPlayer(Player player) {
        Game game = gameManager.getGame();
        Projectile shot = currentShot;

        // Gets current player sender reference
        Sender sender = gameManager.getSenderByPlayer(player);

        Component destroyedComponent = pirates.penaltyShot(game, player, shot, diceResult);

        // Sends two types of messages based on the shot's result
        if (destroyedComponent == null)
            return;

        if (!SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, destroyedComponent.getX(), destroyedComponent.getY(), sender)) {
            gameManager.getGame().getBoard().leaveTravel(player);
            gameManager.broadcastGameMessage(new UpdateOtherTravelersShipMessage(gameManager.getGame().getBoard().getCopyTravelers()));
            gameManager.broadcastGameMessage(new UpdateTrackMessage(GameController.getAllPlayersInTrackCopy(gameManager), gameManager.getGame().getBoard().getTrack()));
        }
    }

    /**
     * Reconnects player to the game
     *
     * @author Alessandro
     * @param player current player
     * @param sender current sender
     */
    @Override
    public void reconnectPlayer(Player player, Sender sender) {
        if (!activePlayers.contains(player))
            return;

        if(phase.equals(EventPhase.ROLL_DICE) && player.equals(leaderPlayer)){
            MessageSenderService.sendMessage(new IncomingProjectileMessage(currentShot), sender);

            if (currentShot.getFrom() == 0 || currentShot.getFrom() == 2) {
                MessageSenderService.sendMessage("RollDiceToFindColumn", sender);

            } else if (currentShot.getFrom() == 1 || currentShot.getFrom() == 3) {
                MessageSenderService.sendMessage("RollDiceToFindRow", sender);
            }

            return;
        }

        player.setIsReady(false, gameManager.getGame());

        // Reset decisions
        removeShieldPlayer(player);
        removeShieldBatteryPlayer(player);

        if(containsHandledPlayer(player)){
            if(player.getSpaceship().getBuildingBoard().checkShipValidityAndFixAliens())
                MessageSenderService.sendMessage("ShotAlreadyHandled", sender);
            else
                MessageSenderService.sendMessage("AskSelectSpaceshipPart", sender);
            return;
        }

        askToUseShieldSinglePlayer(player);
    }
}