package org.progetto.server.controller.events;

import org.progetto.messages.toClient.EventCommon.*;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.controller.SpaceshipController;
import org.progetto.server.model.Board;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.events.Pirates;
import org.progetto.server.model.events.Projectile;
import org.progetto.server.model.events.ProjectileSize;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class PiratesController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private Pirates pirates;
    private ArrayList<Player> activePlayers;
    private float playerFirePower;
    private int requestedBatteries;
    private ArrayList<Player> defeatedPlayers;
    private int diceResult;
    private boolean defeated;
    private ArrayList<Projectile> penaltyShots;
    private ArrayList<Player> protectedPlayers;
    private ArrayList<Player> notProtectedPlayers;
    private ArrayList<Player> discardedBattery;

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
        this.diceResult = 0;
        this.defeated = false;
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
     * @throws RemoteException
     */
    @Override
    public void start() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.START)) {
            phase = EventPhase.ASK_CANNONS;
            askHowManyCannonsToUse();
        }
    }

    /**
     * Asks current player how many double cannons he wants to use
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void askHowManyCannonsToUse() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.ASK_CANNONS)) {

            for (Player player : activePlayers) {

                gameManager.getGame().setActivePlayer(player);

                Spaceship spaceship = player.getSpaceship();

                // Retrieves sender reference
                Sender sender = gameManager.getSenderByPlayer(player);

                // Checks if players is able to win without double cannons
                if (pirates.battleResult(player, spaceship.getNormalShootingPower()) == 1) {
                    phase = EventPhase.REWARD_DECISION;
                    sender.sendMessage(new AcceptRewardCreditsAndPenaltyDaysMessage(pirates.getRewardCredits(), pirates.getPenaltyDays()));

                } else {

                    // Calculates max number of double cannons usable
                    int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

                    // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
                    if (maxUsable == 0) {
                        playerFirePower = spaceship.getNormalShootingPower();

                        phase = EventPhase.BATTLE_RESULT;
                        battleResult(player, sender);

                    } else {
                        sender.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, pirates.getFirePowerRequired()));

                        phase = EventPhase.CANNON_NUMBER;
                    }
                }

                gameManager.getGameThread().resetAndWaitPlayerReady(player);

                // Checks if card got defeated
                if (defeated) {
                    break;
                }
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
     * @param num
     * @param sender
     * @throws RemoteException
     */
    public void receiveHowManyCannonsToUse(Player player, int num, Sender sender) throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.CANNON_NUMBER)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

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

                    sender.sendMessage(new BatteriesToDiscardMessage(num));

                    phase = EventPhase.DISCARDED_BATTERIES;

                } else {
                    sender.sendMessage("IncorrectNumber");
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Receives the coordinates of BatteryStorage component from which remove a battery
     *
     * @author Gabriele
     * @param player
     * @param xBatteryStorage
     * @param yBatteryStorage
     * @param sender
     * @throws RemoteException
     */
    public void receiveDiscardedBattery(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.DISCARDED_BATTERIES)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();
                Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

                if (batteryStorage != null && batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {

                    // Checks if a battery has been discarded
                    if (pirates.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                        requestedBatteries--;
                        sender.sendMessage("BatteryDiscarded");

                        if (requestedBatteries == 0) {
                            phase = EventPhase.BATTLE_RESULT;
                            battleResult(player, sender);

                        } else {
                            sender.sendMessage(new BatteriesToDiscardMessage(requestedBatteries));
                        }

                    } else {
                        sender.sendMessage("NotEnoughBatteries");
                    }

                } else {
                    sender.sendMessage("InvalidCoordinates");
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Check the result of the battle and goes to a new phase based on the result
     *
     * @author Gabriele
     * @param player
     * @param sender
     * @throws RemoteException
     */
    private void battleResult(Player player, Sender sender) throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.BATTLE_RESULT)) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(gameManager.getGame().getActivePlayer())) {

                // Calls the battleResult function
                switch (pirates.battleResult(player, playerFirePower)){
                    case 1:
                        phase = EventPhase.REWARD_DECISION;
                        sender.sendMessage(new AcceptRewardCreditsAndPenaltyDaysMessage(pirates.getRewardCredits(), pirates.getPenaltyDays()));
                        defeated = true;
                        break;

                    case -1:
                        defeatedPlayers.add(player);

                        player.setIsReady(true, gameManager.getGame());
                        gameManager.getGameThread().notifyThread();
                        break;

                    case 0:
                        player.setIsReady(true, gameManager.getGame());
                        gameManager.getGameThread().notifyThread();
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
     * Receives response for rewardPenalty
     *
     * @author Gabriele
     * @param player
     * @param response
     * @param sender
     * @throws RemoteException
     */
    public void receiveRewardDecision(Player player, String response, Sender sender) throws RemoteException {
        if (phase.equals(EventPhase.REWARD_DECISION)) {

            if (player.equals(gameManager.getGame().getActivePlayer())) {
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
     * If the player accepted, he receives the reward and loses the penalty days
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void eventEffect() throws RemoteException {
        if (phase.equals(EventPhase.EFFECT)) {

            Player player = gameManager.getGame().getActivePlayer();
            Board board = gameManager.getGame().getBoard();

            // Event effect applied for single player
            pirates.rewardPenalty(gameManager.getGame().getBoard(), player);

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            sender.sendMessage(new PlayerMovedBackwardMessage(pirates.getPenaltyDays()));
            sender.sendMessage(new PlayerGetsCreditsMessage(pirates.getRewardCredits()));
            gameManager.broadcastGameMessage(new AnotherPlayerMovedBackwardMessage(player.getName(), pirates.getPenaltyDays()));
            gameManager.broadcastGameMessage(new AnotherPlayerGetsCreditsMessage(player.getName(), pirates.getRewardCredits()));

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
        }
    }

    /**
     * Handles defeated players
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void handleDefeatedPlayers() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.HANDLE_DEFEATED_PLAYERS)) {

            for (Projectile shot : penaltyShots) {

                if (defeatedPlayers.isEmpty()) {
                    break;
                }

                // Sends to each defeated player information about incoming shot
                for (Player defeatedPlayer : defeatedPlayers) {
                    Sender sender = gameManager.getSenderByPlayer(defeatedPlayer);

                    sender.sendMessage(new IncomingProjectileMessage(penaltyShots.getFirst().getSize(), penaltyShots.getFirst().getFrom()));
                }

                phase = EventPhase.ASK_ROLL_DICE;
                askToRollDice();

                gameManager.getGameThread().resetAndWaitTravelersReady();

                // Resets elaboration attributes
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
     * @throws RemoteException
     */
    private void askToRollDice() throws RemoteException {
        if (phase.equals(EventPhase.ASK_ROLL_DICE)) {

            // Asks first defeated player to roll dice
            Sender sender = gameManager.getSenderByPlayer(defeatedPlayers.getFirst());

            if (penaltyShots.getFirst().getFrom() == 0 || penaltyShots.getFirst().getFrom() == 2) {
                sender.sendMessage("ThrowDiceToFindColumn");

            } else if (penaltyShots.getFirst().getFrom() == 1 || penaltyShots.getFirst().getFrom() == 3) {
                sender.sendMessage("ThrowDiceToFindRow");
            }

            phase = EventPhase.ROLL_DICE;
        }
    }

    /**
     * Rolls dice and collects the result
     *
     * @author Gabriele
     * @param player
     * @param sender
     * @throws RemoteException
     */
    @Override
    public void rollDice(Player player, Sender sender) throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.ROLL_DICE)) {

            // Checks if the player that calls the methods is also the first defeated player
            if (player.equals(defeatedPlayers.getFirst())) {

                diceResult = player.rollDice();

                sender.sendMessage(new DiceResultMessage(diceResult));
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiceResultMessage(defeatedPlayers.getFirst().getName(), diceResult), sender);

                if (penaltyShots.getFirst().getSize().equals(ProjectileSize.SMALL)) {
                    phase = EventPhase.ASK_SHIELDS;
                    askToUseShields();

                } else {
                    notProtectedPlayers.addAll(defeatedPlayers);

                    phase = EventPhase.HANDLE_SHOT;
                    handleShot();
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Asks defeated players if they want to use shields to protect
     *
     * @author Gabriele
     */
    private void askToUseShields() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.ASK_SHIELDS)) {

            for (Player defeatedPlayer : defeatedPlayers) {

                // Checks if current player has a shield that covers that direction
                boolean hasShield = pirates.checkShields(defeatedPlayer, penaltyShots.getFirst());

                // Asks current defeated player if he wants to use a shield
                Sender sender = gameManager.getSenderByPlayer(defeatedPlayer);

                if (hasShield && defeatedPlayer.getSpaceship().getBatteriesCount() > 0) {
                    sender.sendMessage("AskToUseShield");

                } else {
                    notProtectedPlayers.add(defeatedPlayer);
                    sender.sendMessage("NoShieldAvailable");
                }
            }

            if (notProtectedPlayers.size() == defeatedPlayers.size()) {
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
     * @param player
     * @param response
     * @param sender
     * @throws RemoteException
     * @throws InterruptedException
     */
    public synchronized void receiveShieldDecision(Player player, String response, Sender sender) throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.SHIELD_DECISION)) {

            // Checks if it is not part of non-protected player, and it is not already contained in protected one list
            if (!notProtectedPlayers.contains(player) && !protectedPlayers.contains(player)) {

                String upperCaseResponse = response.toUpperCase();

                switch (upperCaseResponse) {
                    case "YES":
                        protectedPlayers.add(player);
                        discardedBattery.add(player);
                        break;

                    case "NO":
                        notProtectedPlayers.add(player);
                        break;

                    default:
                        sender.sendMessage("IncorrectResponse");
                        break;
                }

                // Checks that every player has given his preference
                if (notProtectedPlayers.size() + protectedPlayers.size() == defeatedPlayers.size()) {

                    if (!protectedPlayers.isEmpty()) {

                        // Asks for a battery to each protected player
                        for (Player protectedPlayer : protectedPlayers) {
                            Sender senderProtected = gameManager.getSenderByPlayer(protectedPlayer);

                            senderProtected.sendMessage(new BatteriesToDiscardMessage(1));
                        }

                        phase = EventPhase.SHIELD_BATTERY;

                    } else {
                        phase = EventPhase.HANDLE_SHOT;
                        handleShot();
                    }
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Receives the coordinates of BatteryStorage component from which remove a battery to activate shield
     *
     * @author Gabriele
     * @param player
     * @param xBatteryStorage
     * @param yBatteryStorage
     * @param sender
     * @throws RemoteException
     */
    public synchronized void receiveShieldBattery(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.SHIELD_BATTERY)) {

            // Checks if the player that calls the methods has to discard a battery to activate a shield
            if (discardedBattery.contains(player)) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix();
                Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

                if (batteryStorage != null && batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {

                    // Checks if a battery has been discarded
                    if (pirates.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {

                        discardedBattery.remove(player);
                        sender.sendMessage("BatteryDiscarded");

                        if (discardedBattery.isEmpty()) {
                            phase = EventPhase.HANDLE_SHOT;
                            handleShot();
                        }

                    } else {
                        sender.sendMessage("NotEnoughBatteries");
                    }

                } else {
                    sender.sendMessage("InvalidCoordinates");
                }

            } else {
                sender.sendMessage("NotYourTurn");
            }

        } else {
            sender.sendMessage("IncorrectPhase");
        }
    }

    /**
     * Handles current shot
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void handleShot() throws RemoteException, InterruptedException {
        if (phase.equals(EventPhase.HANDLE_SHOT)) {

            Game game = gameManager.getGame();
            Projectile shot = penaltyShots.getFirst();

            // Set as ready not defeated players
            for (Player player : activePlayers) {
                if (!defeatedPlayers.contains(player)) {
                    player.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                }
            }

            // For each non-protected player handles penalty shot
            for (Player notProtectedPlayer : notProtectedPlayers) {
                Component destroyedComponent = pirates.penaltyShot(game, notProtectedPlayer, shot, diceResult);

                // Gets current player sender reference
                Sender sender = gameManager.getSenderByPlayer(notProtectedPlayer);

                // Sends two types of messages based on the shot's result
                if (destroyedComponent != null) {
                    // TODO: handle waiting in case of needed decision by player on which part of the ship to hold
                    SpaceshipController.destroyComponentAndCheckValidity(gameManager, notProtectedPlayer, destroyedComponent.getX(), destroyedComponent.getY(), sender);

                } else {
                    gameManager.broadcastGameMessage("NothingGotDestroyed");

                    notProtectedPlayer.setIsReady(true, gameManager.getGame());
                    gameManager.getGameThread().notifyThread();
                }
            }
        }
    }
}