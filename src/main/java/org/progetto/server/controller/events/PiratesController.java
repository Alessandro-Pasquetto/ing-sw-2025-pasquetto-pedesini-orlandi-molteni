package org.progetto.server.controller.events;

import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.AffectedComponentMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
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
    private final ArrayList<Player> defeatedPlayers;
    private int diceResult;
    private boolean defeated;
    private final ArrayList<Projectile> penaltyShots;
    private Projectile currentShot;
    private final ArrayList<Player> notAffectedPlayers;
    private final ArrayList<Player> protectedPlayers;
    private final ArrayList<Player> notProtectedPlayers;
    private final ArrayList<Player> discardedBattery;

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
        this.defeatedPlayers = new ArrayList<>();
        this.penaltyShots = new ArrayList<>(pirates.getPenaltyShots());
        this.currentShot = null;
        this.diceResult = 0;
        this.defeated = false;
        this.notAffectedPlayers = new ArrayList<>();
        this.protectedPlayers = new ArrayList<>();
        this.notProtectedPlayers = new ArrayList<>();
        this.discardedBattery = new ArrayList<>();
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
        if (phase.equals(EventPhase.ASK_CANNONS)) {

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
                if (pirates.battleResult(player, spaceship.getNormalShootingPower()) == 1) {
                    phase = EventPhase.REWARD_DECISION;
                    MessageSenderService.sendOptional("YouWonBattle", sender);
                    gameManager.broadcastGameMessageToOthers(new AnotherPlayerWonBattleMessage(player.getName()), sender);

                    MessageSenderService.sendOptional(new AcceptRewardCreditsAndPenaltyDaysMessage(pirates.getRewardCredits(), pirates.getPenaltyDays()), sender);

                    gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                    gameManager.getGameThread().resetAndWaitTravelerReady(player);
                    continue;
                }

                // Calculates max number of double cannons usable
                int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

                // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
                if (maxUsable == 0) {
                    playerFirePower = spaceship.getNormalShootingPower();

                    if (pirates.battleResult(player, spaceship.getNormalShootingPower()) == -1) {
                        MessageSenderService.sendOptional("YouLostBattle", sender);
                        gameManager.broadcastGameMessageToOthers(new AnotherPlayerLostBattleMessage(player.getName()), sender);

                        defeatedPlayers.add(player);

                    } else {
                        MessageSenderService.sendOptional("YouDrewBattle", sender);
                        gameManager.broadcastGameMessageToOthers(new AnotherPlayerDrewBattleMessage(player.getName()), sender);
                    }
                    continue;

                } else {
                    MessageSenderService.sendOptional(new HowManyDoubleCannonsMessage(maxUsable, pirates.getFirePowerRequired(), player.getSpaceship().getNormalShootingPower()), sender);
                    phase = EventPhase.CANNON_NUMBER;
                }

                gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

                gameManager.getGameThread().resetAndWaitTravelerReady(player);
            }

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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
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

            MessageSenderService.sendOptional(new BatteriesToDiscardMessage(num), sender);

            phase = EventPhase.DISCARDED_BATTERIES;

        } else {
            MessageSenderService.sendOptional("IncorrectNumber", sender);
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();
            MessageSenderService.sendOptional(new HowManyDoubleCannonsMessage(maxUsable, pirates.getFirePowerRequired(), player.getSpaceship().getNormalShootingPower()), sender);
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
            case DISCARDED_BATTERIES:
                if (!player.equals(gameManager.getGame().getActivePlayer())) {
                    MessageSenderService.sendOptional("NotYourTurn", sender);
                    return;
                }
                break;

            case SHIELD_BATTERY:
                if (!discardedBattery.contains(player)) {
                    MessageSenderService.sendOptional("NotYourTurn", sender);
                    return;
                }
                break;

            default:
                MessageSenderService.sendOptional("IncorrectPhase", sender);
                return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xBatteryStorage < 0 || yBatteryStorage < 0 || yBatteryStorage >= spaceshipMatrix.length || xBatteryStorage >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendOptional("InvalidCoordinates", sender);

            if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBatteries), sender);

            } else if (phase.equals(EventPhase.SHIELD_BATTERY)) {
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(1), sender);
            }
            return;
        }

        Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

        // Checks if component is a battery storage
        if (batteryStorage == null || !batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {
            MessageSenderService.sendOptional("InvalidComponent", sender);

            if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBatteries), sender);

            } else if (phase.equals(EventPhase.SHIELD_BATTERY)) {
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(1), sender);
            }
            return;
        }

        if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {

            // Checks if a battery has been discarded
            if (pirates.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                requestedBatteries--;

                MessageSenderService.sendOptional(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

                if (requestedBatteries == 0) {
                    phase = EventPhase.BATTLE_RESULT;
                    battleResult(player, sender);

                } else {
                    MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBatteries), sender);
                }

            } else {
                MessageSenderService.sendOptional("BatteryNotDiscarded", sender);
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBatteries), sender);
            }

        } else if (phase.equals(EventPhase.SHIELD_BATTERY)) {

            // Checks if a battery has been discarded
            if (pirates.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                discardedBattery.remove(player);

                MessageSenderService.sendOptional(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

                MessageSenderService.sendOptional("YouAreSafe", sender);

                if (discardedBattery.isEmpty()) {
                    phase = EventPhase.HANDLE_SHOT;
                    handleShot();
                }

            } else {
                MessageSenderService.sendOptional("BatteryNotDiscarded", sender);
                MessageSenderService.sendOptional(new BatteriesToDiscardMessage(requestedBatteries), sender);
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
        if (phase.equals(EventPhase.BATTLE_RESULT)) {

            // Calls the battleResult function
            switch (pirates.battleResult(player, playerFirePower)) {
                case 1:
                    phase = EventPhase.REWARD_DECISION;
                    MessageSenderService.sendOptional("YouWonBattle", sender);
                    gameManager.broadcastGameMessageToOthers(new AnotherPlayerWonBattleMessage(player.getName()), sender);

                    MessageSenderService.sendOptional(new AcceptRewardCreditsAndPenaltyDaysMessage(pirates.getRewardCredits(), pirates.getPenaltyDays()), sender);
                    defeated = true;
                    break;

                case -1:
                    defeatedPlayers.add(player);

                    MessageSenderService.sendOptional("YouLostBattle", sender);
                    gameManager.broadcastGameMessageToOthers(new AnotherPlayerLostBattleMessage(player.getName()), sender);

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                    break;

                case 0:
                    MessageSenderService.sendOptional("YouDrewBattle", sender);
                    gameManager.broadcastGameMessageToOthers(new AnotherPlayerDrewBattleMessage(player.getName()), sender);

                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                    break;
            }
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        // Checks if current player is active one
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
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
                MessageSenderService.sendOptional("IncorrectResponse", sender);
                MessageSenderService.sendOptional(new AcceptRewardCreditsAndPenaltyDaysMessage(pirates.getRewardCredits(), pirates.getPenaltyDays()), sender);
                break;
        }
    }

    /**
     * If the player accepted, he receives the reward and loses the penalty days
     *
     * @author Gabriele
     */
    private void eventEffect() {
        if (phase.equals(EventPhase.EFFECT)) {

            Player player = gameManager.getGame().getActivePlayer();

            // Event effect applied for single player
            pirates.rewardPenalty(gameManager.getGame().getBoard(), player);

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            MessageSenderService.sendOptional(new PlayerMovedBackwardMessage(pirates.getPenaltyDays()), sender);
            MessageSenderService.sendOptional(new PlayerGetsCreditsMessage(pirates.getRewardCredits()), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedBackwardMessage(player.getName(), pirates.getPenaltyDays()), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerGetsCreditsMessage(player.getName(), pirates.getRewardCredits()), sender);

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
        }
    }

    /**
     * Handles defeated players
     *
     * @author Gabriele
     * @throws InterruptedException
     */
    private void handleDefeatedPlayers() throws InterruptedException {
        if (phase.equals(EventPhase.HANDLE_DEFEATED_PLAYERS)) {

            gameManager.broadcastGameMessage("ResetActivePlayer");

            for (Projectile shot : penaltyShots) {

                if (defeatedPlayers.isEmpty()) {
                    break;
                }

                currentShot = shot;

                // Sends to each defeated player information about incoming shot
                for (Player defeatedPlayer : defeatedPlayers) {
                    Sender sender = gameManager.getSenderByPlayer(defeatedPlayer);

                    MessageSenderService.sendOptional(new IncomingProjectileMessage(currentShot), sender);
                }

                phase = EventPhase.ASK_ROLL_DICE;
                askToRollDice();

                gameManager.getGameThread().resetAndWaitTravelersReady();

                // Resets elaboration attributes
                notAffectedPlayers.clear();
                protectedPlayers.clear();
                notProtectedPlayers.clear();
                discardedBattery.clear();
            }
        }
    }

    /**
     * Asks first penalized player to roll dice
     *
     * @author Gabriele
     */
    private void askToRollDice() {
        if (phase.equals(EventPhase.ASK_ROLL_DICE)) {

            // Asks first defeated player to roll dice
            Sender sender = gameManager.getSenderByPlayer(defeatedPlayers.getFirst());

            if (currentShot.getFrom() == 0 || currentShot.getFrom() == 2) {
                MessageSenderService.sendOptional("RollDiceToFindColumn", sender);

            } else if (currentShot.getFrom() == 1 || currentShot.getFrom() == 3) {
                MessageSenderService.sendOptional("RollDiceToFindRow", sender);
            }

            phase = EventPhase.ROLL_DICE;
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the first defeated player
        if (!player.equals(defeatedPlayers.getFirst())) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
            return;
        }

        diceResult = player.rollDice();

        MessageSenderService.sendOptional(new DiceResultMessage(diceResult), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiceResultMessage(defeatedPlayers.getFirst().getName(), diceResult), sender);

        // Delay to show the dice result
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Checks projectile dimensions
        if (currentShot.getSize().equals(ProjectileSize.SMALL)) {
            phase = EventPhase.ASK_SHIELDS;
            askToUseShields();

        } else {
            notProtectedPlayers.addAll(defeatedPlayers);

            phase = EventPhase.HANDLE_SHOT;
            handleShot();
        }
    }

    /**
     * Asks defeated players if they want to use shields to protect
     *
     * @author Gabriele
     */
    private void askToUseShields() {
        if (phase.equals(EventPhase.ASK_SHIELDS)) {

            ArrayList<Player> playersToRemove = new ArrayList<>();

            for (Player defeatedPlayer : defeatedPlayers) {

                // Asks current defeated player if he wants to use a shield
                Sender sender = gameManager.getSenderByPlayer(defeatedPlayer);

                // Finds impact component
                Component affectedComponent = pirates.penaltyShot(gameManager.getGame(), defeatedPlayer, currentShot, diceResult);

                // Checks if there is any affected component
                if (affectedComponent == null) {
                    MessageSenderService.sendOptional("NoComponentHit", sender);
                    notAffectedPlayers.add(defeatedPlayer);
                    continue;
                }

                MessageSenderService.sendOptional(new AffectedComponentMessage(affectedComponent.getX(), affectedComponent.getY()), sender);

                // Checks if current player has a shield that covers that direction
                boolean hasShield = pirates.checkShields(defeatedPlayer, currentShot);

                if (hasShield && defeatedPlayer.getSpaceship().getBatteriesCount() > 0) {
                    MessageSenderService.sendOptional("AskToUseShield", sender);

                } else {
                    notProtectedPlayers.add(defeatedPlayer);
                    MessageSenderService.sendOptional("NoShieldAvailable", sender);
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        // Checks if it is not part of non-protected player, and it is not already contained in protected one list
        if (notProtectedPlayers.contains(player) || protectedPlayers.contains(player)) {
            MessageSenderService.sendOptional("NotYourTurn", sender);
            return;
        }

        String upperCaseResponse = response.toUpperCase();

        switch (upperCaseResponse) {
            case "YES":
                protectedPlayers.add(player);
                discardedBattery.add(player);
                MessageSenderService.sendOptional("YouAnsweredYes", sender);
                break;

            case "NO":
                notProtectedPlayers.add(player);
                MessageSenderService.sendOptional("YouAnsweredNo", sender);
                break;

            default:
                MessageSenderService.sendOptional("IncorrectResponse", sender);
                MessageSenderService.sendOptional("AskToUseShield", sender);
                break;
        }

        // Checks that every player has given his preference
        if (notProtectedPlayers.size() + protectedPlayers.size() == defeatedPlayers.size()) {

            if (!protectedPlayers.isEmpty()) {

                // Asks for a battery to each protected player
                for (Player protectedPlayer : protectedPlayers) {
                    Sender senderProtected = gameManager.getSenderByPlayer(protectedPlayer);

                    MessageSenderService.sendOptional(new BatteriesToDiscardMessage(1), senderProtected);
                }

                phase = EventPhase.SHIELD_BATTERY;

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
        if (phase.equals(EventPhase.HANDLE_SHOT)) {

            Game game = gameManager.getGame();
            Projectile shot = currentShot;

            // Set as ready not defeated players
            for (Player player : activePlayers) {
                if (!defeatedPlayers.contains(player)) {
                    player.setIsReady(true, gameManager.getGame());
                }
            }

            // Set as ready not affected players
            for (Player player : notAffectedPlayers) {
                player.setIsReady(true, gameManager.getGame());
            }

            // Set as ready protected players
            for (Player player : protectedPlayers) {
                player.setIsReady(true, gameManager.getGame());
            }

            // For each non-protected player handles penalty shot
            for (Player player : notProtectedPlayers) {
                Component destroyedComponent = pirates.penaltyShot(game, player, shot, diceResult);

                // Gets current player sender reference
                Sender sender = gameManager.getSenderByPlayer(player);

                // Sends two types of messages based on the shot's result
                if (destroyedComponent == null) {
                    MessageSenderService.sendOptional("NothingGotDestroyed", sender);
                    player.setIsReady(true, gameManager.getGame());
                    continue;
                }

                if (SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, destroyedComponent.getX(), destroyedComponent.getY(), sender)){
                    player.setIsReady(true, gameManager.getGame());

                } else
                    MessageSenderService.sendOptional("AskSelectSpaceshipPart", sender);
            }

            gameManager.getGameThread().notifyThread();
        }
    }
}