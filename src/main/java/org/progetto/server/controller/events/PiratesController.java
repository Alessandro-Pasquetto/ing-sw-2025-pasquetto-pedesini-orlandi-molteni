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
    private final ArrayList<Player> activePlayers;
    private float playerFirePower;
    private int requestedBatteries;
    private int diceResult;
    private boolean defeated;
    private final ArrayList<Projectile> penaltyShots;
    private Projectile currentShot;
    private final ArrayList<Player> defeatedPlayers;
    private final ArrayList<Player> notAffectedPlayers;
    private final ArrayList<Player> protectedPlayers;
    private final ArrayList<Player> notProtectedPlayers;

    private final ArrayList<BatteryStorage> batteryStorages;
    private final ArrayList<Player> discardedBatteryPlayers;
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
        this.notAffectedPlayers = new ArrayList<>();
        this.protectedPlayers = new ArrayList<>();
        this.notProtectedPlayers = new ArrayList<>();

        this.batteryStorages = new ArrayList<>();
        this.discardedBatteryPlayers = new ArrayList<>();
        this.handledPlayers = new ArrayList<>();
    }

    private void addDefeatedPlayer(Player player) {
        synchronized (defeatedPlayers) {
            defeatedPlayers.add(player);
        }
    }

    private void addProtectedPlayer(Player player) {
        synchronized (protectedPlayers) {
            protectedPlayers.add(player);
        }
    }

    private void addNotProtectedPlayer(Player player) {
        synchronized (notProtectedPlayers) {
            notProtectedPlayers.add(player);
        }
    }

    private void addDiscardedBatteryPlayer(Player player) {
        synchronized (discardedBatteryPlayers) {
            discardedBatteryPlayers.add(player);
        }
    }

    @Override
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

    private void removeProtectedPlayer(Player player) {
        synchronized (protectedPlayers) {
            protectedPlayers.remove(player);
        }
    }

    private void removeNotProtectedPlayer(Player player) {
        synchronized (notProtectedPlayers) {
            notProtectedPlayers.remove(player);
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

    private boolean containsDefeatedPlayer(Player player) {
        synchronized (defeatedPlayers) {
            return defeatedPlayers.contains(player);
        }
    }

    private boolean containsProtectedPlayer(Player player) {
        synchronized (protectedPlayers) {
            return protectedPlayers.contains(player);
        }
    }

    private boolean containsNotProtectedPlayer(Player player) {
        synchronized (notProtectedPlayers) {
            return notProtectedPlayers.contains(player);
        }
    }

    private boolean containsDiscardedBatteryPlayer(Player player) {
        synchronized (discardedBatteryPlayers) {
            return discardedBatteryPlayers.contains(player);
        }
    }

    private boolean containsHandledPlayer(Player player) {
        synchronized (handledPlayers) {
            return handledPlayers.contains(player);
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

            gameManager.getGame().setActivePlayer(player);

            Spaceship spaceship = player.getSpaceship();

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            // Checks if card got defeated
            if (defeated) {
                gameManager.broadcastGameMessage("RaidersDefeated");
                break;
            }

            // Checks if players is able to win without double cannons
            if (pirates.battleResult(spaceship.getNormalShootingPower()) == 1) {
                phase = EventPhase.REWARD_DECISION;
                MessageSenderService.sendMessage("YouWonBattle", sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerWonBattleMessage(player.getName()), sender);
                defeated = true;

                MessageSenderService.sendMessage(new AcceptRewardCreditsAndPenaltyDaysMessage(pirates.getRewardCredits(), pirates.getPenaltyDays()), sender);

                gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                gameManager.getGameThread().resetAndWaitTravelerReady(player);

                continue;
            }

            // Calculates max number of double cannons usable
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

            // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
            if (maxUsable == 0) {
                playerFirePower = spaceship.getNormalShootingPower();

                if (pirates.battleResult(spaceship.getNormalShootingPower()) == -1) {
                    MessageSenderService.sendMessage("YouLostBattle", sender);
                    gameManager.broadcastGameMessageToOthers(new AnotherPlayerLostBattleMessage(player.getName()), sender);

                    addDefeatedPlayer(player);

                } else {
                    MessageSenderService.sendMessage("YouDrewBattle", sender);
                    gameManager.broadcastGameMessageToOthers(new AnotherPlayerDrewBattleMessage(player.getName()), sender);
                }

            } else {
                phase = EventPhase.CANNON_NUMBER;
                MessageSenderService.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, pirates.getFirePowerRequired(), player.getSpaceship().getNormalShootingPower()), sender);

                gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                gameManager.getGameThread().resetAndWaitTravelerReady(player);

                // If the player is disconnected and slavers are not defeated (disconnection in rewardDecision)
                if(!player.getIsReady() && !defeated){
                    handleDisconnection(player, spaceship, sender);
                }
            }
        }

        if(!defeatedPlayers.isEmpty()){
            phase = EventPhase.HANDLE_DEFEATED_PLAYERS;
            handleDefeatedPlayers();
        }
    }

    private void handleDisconnection(Player player, Spaceship spaceship, Sender sender) {
        if(pirates.battleResult(spaceship.getNormalShootingPower()) == -1){
            MessageSenderService.sendMessage("YouLostBattle", sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerLostBattleMessage(player.getName()), sender);

            addDefeatedPlayer(player);
        }else{
            MessageSenderService.sendMessage("YouDrewBattle", sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerDrewBattleMessage(player.getName()), sender);
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

            phase = EventPhase.BATTLE_RESULT;
            battleResult(player, sender);

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
    public synchronized void receiveDiscardedBatteries(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) {
        switch (phase) {
            case DISCARDED_BATTERIES: // Sequential
                if (!player.equals(gameManager.getGame().getActivePlayer())) {
                    MessageSenderService.sendMessage("NotYourTurn", sender);
                    return;
                }
                break;

            case SHIELD_BATTERY: // Parallel
                if (!discardedBatteryPlayers.contains(player)) {
                    MessageSenderService.sendMessage("NotYourTurn", sender);
                    return;
                }
                break;

            default:
                MessageSenderService.sendMessage("IncorrectPhase", sender);
                return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xBatteryStorage < 0 || yBatteryStorage < 0 || yBatteryStorage >= spaceshipMatrix.length || xBatteryStorage >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendMessage("InvalidCoordinates", sender);

            if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

            } else if (phase.equals(EventPhase.SHIELD_BATTERY)) {
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

            } else if (phase.equals(EventPhase.SHIELD_BATTERY)) {
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(1), sender);
            }
            return;
        }

        BatteryStorage batteryStorage = (BatteryStorage) batteryStorageComp;

        if(batteryStorage.getItemsCount() == 0) {
            MessageSenderService.sendMessage("EmptyBatteryStorage", sender);

            if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);

            } else if (phase.equals(EventPhase.SHIELD_BATTERY)) {
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

                // Update spaceship to remove highlight components when it's not my turn.
                // For others, it's used to reload the spaceship in case they got disconnected while it was discarding.
                gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));

                phase = EventPhase.BATTLE_RESULT;
                battleResult(player, sender);

            } else {
                MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);
            }


        } else if (phase.equals(EventPhase.SHIELD_BATTERY)) {

            removeDiscardedBatteryPlayer(player);
            batteryStorage.decrementItemsCount(player.getSpaceship(), 1);

            MessageSenderService.sendMessage(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

            // Update spaceship to remove highlight components when it's not my turn.
            // For others, it's used to reload the spaceship in case they got disconnected while it was discarding.
            gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));

            MessageSenderService.sendMessage("YouAreSafe", sender);

            if (discardedBatteryPlayers.isEmpty()) {
                phase = EventPhase.HANDLE_SHOT;
                handleShot();
            }
        }
    }

    /**
     * Check the result of the battle and goes to a new phase based on the result
     *
     * @author Gabriele
     * @param player current player
     * @param sender current sender
     */
    private void battleResult(Player player, Sender sender) {
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
                break;

            case -1:
                addDefeatedPlayer(player);

                MessageSenderService.sendMessage("YouLostBattle", sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerLostBattleMessage(player.getName()), sender);

                player.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();
                break;

            case 0:
                MessageSenderService.sendMessage("YouDrewBattle", sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerDrewBattleMessage(player.getName()), sender);

                player.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();
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

        for (Projectile shot : penaltyShots) {
            // Resets elaboration attributes
            notAffectedPlayers.clear();
            protectedPlayers.clear();
            notProtectedPlayers.clear();
            discardedBatteryPlayers.clear();

            currentShot = shot;

            // Sends to each defeated player information about incoming shot
            for (Player defeatedPlayer : defeatedPlayers) {
                Sender sender = gameManager.getSenderByPlayer(defeatedPlayer);

                MessageSenderService.sendMessage(new IncomingProjectileMessage(currentShot), sender);
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

            handleShots();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " was interrupted during sleep.");
                e.printStackTrace();
            }
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

        // Asks first defeated player to roll dice
        Sender sender = gameManager.getSenderByPlayer(defeatedPlayers.getFirst());

        phase = EventPhase.ROLL_DICE;

        if (currentShot.getFrom() == 0 || currentShot.getFrom() == 2) {
            MessageSenderService.sendMessage("RollDiceToFindColumn", sender);

        } else if (currentShot.getFrom() == 1 || currentShot.getFrom() == 3) {
            MessageSenderService.sendMessage("RollDiceToFindRow", sender);
        }

        gameManager.getGameThread().resetAndWaitTravelerReady(defeatedPlayers.getFirst());

        // If the player is disconnected
        if(!defeatedPlayers.getFirst().getIsReady()){
            rollDice(defeatedPlayers.getFirst(), sender);
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
        if (!player.equals(defeatedPlayers.getFirst())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        diceResult = player.rollDice();

        MessageSenderService.sendMessage(new DiceResultMessage(diceResult), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiceResultMessage(defeatedPlayers.getFirst().getName(), diceResult), sender);

        player.setIsReady(true, gameManager.getGame());
        gameManager.getGameThread().notifyThread();
    }

    private void handleShots(){
        // Checks projectile dimensions
        if (currentShot.getSize().equals(ProjectileSize.SMALL)) {
            phase = EventPhase.ASK_SHIELDS;
            askToUseShields();

        } else {
            notProtectedPlayers.addAll(defeatedPlayers);

            phase = EventPhase.HANDLE_SHOT;
            handleShot();
        }

        try {
            gameManager.getGameThread().waitConnectedTravelersReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //todo

        /*
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
                handleCurrentMeteorForDisconnected(player);
            }
        }

         */

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

            // Asks current defeated player if he wants to use a shield
            Sender sender = gameManager.getSenderByPlayer(defeatedPlayer);

            // Finds impact component
            Component affectedComponent = pirates.penaltyShot(gameManager.getGame(), defeatedPlayer, currentShot, diceResult);

            // Checks if there is any affected component
            if (affectedComponent == null) {
                MessageSenderService.sendMessage("NoComponentHit", sender);
                notAffectedPlayers.add(defeatedPlayer);
                continue;
            }

            MessageSenderService.sendMessage(new AffectedComponentMessage(affectedComponent.getX(), affectedComponent.getY()), sender);

            // Checks if current player has a shield that covers that direction
            boolean hasShield = pirates.checkShields(defeatedPlayer, currentShot);

            if (hasShield && defeatedPlayer.getSpaceship().getBatteriesCount() > 0) {
                MessageSenderService.sendMessage("AskToUseShield", sender);

            } else {
                notProtectedPlayers.add(defeatedPlayer);
                MessageSenderService.sendMessage("NoShieldAvailable", sender);
            }
        }

        // Checks if there are any players that has to make a decision about shield usage
        if ((notProtectedPlayers.size() + notAffectedPlayers.size()) == defeatedPlayers.size()) {
            phase = EventPhase.HANDLE_SHOT;
            handleShot();
        } else {
            phase = EventPhase.SHIELD_DECISION;
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
    public synchronized void receiveProtectionDecision(Player player, String response, Sender sender) {
        if (!phase.equals(EventPhase.SHIELD_DECISION)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if it is not part of non-protected player, and it is not already contained in protected one list
        if (containsNotProtectedPlayer(player) || protectedPlayers.contains(player)) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        String upperCaseResponse = response.toUpperCase();

        switch (upperCaseResponse) {
            case "YES":
                addProtectedPlayer(player);
                addDiscardedBatteryPlayer(player);
                MessageSenderService.sendMessage("YouAnsweredYes", sender);
                break;

            case "NO":
                addNotProtectedPlayer(player);
                MessageSenderService.sendMessage("YouAnsweredNo", sender);
                break;

            default:
                MessageSenderService.sendMessage("IncorrectResponse", sender);
                MessageSenderService.sendMessage("AskToUseShield", sender);
                break;
        }

        // Checks that every player has given his preference
        if (notAffectedPlayers.size() + notProtectedPlayers.size() + protectedPlayers.size() == defeatedPlayers.size()) {

            if (!protectedPlayers.isEmpty()) {

                phase = EventPhase.SHIELD_BATTERY;

                // Asks for a battery to each protected player
                for (Player protectedPlayer : protectedPlayers) {
                    Sender senderProtected = gameManager.getSenderByPlayer(protectedPlayer);

                    MessageSenderService.sendMessage(new BatteriesToDiscardMessage(1), senderProtected);
                }

            } else {
                phase = EventPhase.HANDLE_SHOT;
                handleShot();
            }
        }
    }

    /**
     * Handles current shot
     *
     * @author Gabriele
     */
    private void handleShot() {
        if (!phase.equals(EventPhase.HANDLE_SHOT))
            throw new IllegalStateException("IncorrectPhase");

        Game game = gameManager.getGame();
        Projectile shot = currentShot;

        // Set as ready not defeated players
        for (Player player : activePlayers) {
            if (!defeatedPlayers.contains(player)) {
                player.setIsReady(true, gameManager.getGame());
                handledPlayers.add(player);
            }
        }

        // Set as ready not affected players
        for (Player player : notAffectedPlayers) {
            player.setIsReady(true, gameManager.getGame());
            handledPlayers.add(player);
        }

        // Set as ready protected players
        for (Player player : protectedPlayers) {
            player.setIsReady(true, gameManager.getGame());
            handledPlayers.add(player);
        }

        // For each non-protected player handles penalty shot
        for (Player player : notProtectedPlayers) {
            Component destroyedComponent = pirates.penaltyShot(game, player, shot, diceResult);

            // Gets current player sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            // Sends two types of messages based on the shot's result
            if (destroyedComponent == null) {
                MessageSenderService.sendMessage("NothingGotDestroyed", sender);
                player.setIsReady(true, gameManager.getGame());
                handledPlayers.add(player);
                continue;
            }

            if (SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, destroyedComponent.getX(), destroyedComponent.getY(), sender)){
                player.setIsReady(true, gameManager.getGame());
                handledPlayers.add(player);

            } else
                MessageSenderService.sendMessage("AskSelectSpaceshipPart", sender);
        }

        gameManager.getGameThread().notifyThread();
    }
}