package org.progetto.server.controller.events;

import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.EventCommon.AnotherPlayerMovedBackwardMessage;
import org.progetto.messages.toClient.EventCommon.IncomingProjectileMessage;
import org.progetto.messages.toClient.EventCommon.PlayerDefeatedMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
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

    private GameManager gameManager;
    private Pirates pirates;
    private String phase;
    private int currPlayer;
    private ArrayList<Player> activePlayers;
    private float playerFirePower;
    private int requestedBatteries;
    private ArrayList<Player> defeatedPlayers;
    private int diceResult;
    private ArrayList<Projectile> penaltyShots;
    private ArrayList<Player> shieldProtectedPlayers;
    private ArrayList<Player> shieldNotProtectedPlayers;
    private ArrayList<Player> discardedBatteryForShield;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PiratesController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.pirates = (Pirates) gameManager.getGame().getActiveEventCard();
        this.phase = "START";
        this.currPlayer = 0;
        this.activePlayers = gameManager.getGame().getBoard().getActivePlayers();
        this.playerFirePower = 0;
        this.requestedBatteries = 0;
        this.defeatedPlayers = new ArrayList<>();
        this.penaltyShots = new ArrayList<>(pirates.getPenaltyShots());
        this.diceResult = 0;
        this.shieldProtectedPlayers = new ArrayList<>();
        this.shieldNotProtectedPlayers = new ArrayList<>();
        this.discardedBatteryForShield = new ArrayList<>();
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
    public void start() throws RemoteException {
        phase = "ASK_CANNONS";
        askHowManyCannonsToUse();
    }

    /**
     * Asks current player how many double cannons he wants to use
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void askHowManyCannonsToUse() throws RemoteException {
        if (phase.equals("ASK_CANNONS")) {

            Player player = activePlayers.get(currPlayer);
            Spaceship spaceship = player.getSpaceship();

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            // Checks if players is able to win without double cannons
            if (pirates.battleResult(player, spaceship.getNormalShootingPower()) == 1) {
                phase = "REWARD_DECISION";
                sender.sendMessage(new AcceptRewardCreditsAndPenaltyDaysMessage(pirates.getRewardCredits(), pirates.getPenaltyDays()));
            }

            // Calculates max number of double cannons usable
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

            // If he can't use any double cannon, apply event effect; otherwise, ask how many he wants to use
            if (maxUsable == 0) {
                playerFirePower = spaceship.getNormalShootingPower();

                phase = "BATTLE_RESULT";
                battleResult(player, sender);

            } else {
                sender.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, pirates.getFirePowerRequired()));

                phase = "CANNON_NUMBER";
            }
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
    public void receiveHowManyCannonsToUse(Player player, int num, Sender sender) throws RemoteException {
        if (phase.equals("CANNON_NUMBER")) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                Spaceship spaceship = player.getSpaceship();

                // Player doesn't want to use double cannons
                if (num == 0) {
                    playerFirePower = player.getSpaceship().getNormalShootingPower();

                    phase = "BATTLE_RESULT";
                    battleResult(player, sender);

                } else if (num <= (spaceship.getFullDoubleCannonCount() + spaceship.getHalfDoubleCannonCount()) && num <= spaceship.getBatteriesCount() && num > 0) {
                    requestedBatteries = num;

                    // Updates player's firepower based on his decision
                    if (num <= spaceship.getFullDoubleCannonCount()) {
                        playerFirePower = spaceship.getNormalShootingPower() + 2 * num;
                    } else {
                        playerFirePower = spaceship.getFullDoubleCannonCount() + 2 * spaceship.getFullDoubleCannonCount() + (num - spaceship.getFullDoubleCannonCount());
                    }

                    sender.sendMessage(new BatteriesToDiscardMessage(num));

                    phase = "DISCARDED_BATTERIES";

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
    public void receiveDiscardedBattery(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException {
        if (phase.equals("DISCARDED_BATTERIES")) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

                if (batteryStorage != null && batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {

                    // Checks if a battery has been discarded
                    if (pirates.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {
                        requestedBatteries--;
                        sender.sendMessage("BatteryDiscarded");

                        if (requestedBatteries == 0) {
                            phase = "BATTLE_RESULT";
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
    private void battleResult(Player player, Sender sender) throws RemoteException {
        if (phase.equals("BATTLE_RESULT")) {

            // Checks if the player that calls the methods is also the current one in the controller
            if (player.equals(activePlayers.get(currPlayer))) {

                // Calls the battleResult function
                switch (pirates.battleResult(player, playerFirePower)){
                    case 1:
                        phase = "REWARD_DECISION";
                        sender.sendMessage(new AcceptRewardCreditsAndPenaltyDaysMessage(pirates.getRewardCredits(), pirates.getPenaltyDays()));
                        break;

                    case -1:
                        defeatedPlayers.add(player);

                        // Next player
                        if (currPlayer < activePlayers.size()) {
                            currPlayer++;
                            phase = "ASK_CANNONS";
                            askHowManyCannonsToUse();
                        } else {
                            phase = "HANDLE_DEFEATED_PLAYERS";
                            handleDefeatedPlayers();
                        }
                        break;

                    case 0:
                        // Next player
                        if (currPlayer < activePlayers.size()) {
                            currPlayer++;
                            phase = "ASK_CANNONS";
                            askHowManyCannonsToUse();
                        } else {
                            phase = "HANDLE_DEFEATED_PLAYERS";
                            handleDefeatedPlayers();
                        }
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
        if (phase.equals("REWARD_DECISION")) {

            if (player.equals(activePlayers.get(currPlayer))) {
                String upperCaseResponse = response.toUpperCase();

                switch (upperCaseResponse) {
                    case "YES":
                        phase = "EFFECT";
                        eventEffect();
                        break;

                    case "NO":
                        phase = "END";
                        end();
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
        if (phase.equals("EFFECT")) {
            Player player = activePlayers.get(currPlayer);
            Board board = gameManager.getGame().getBoard();

            // Event effect applied for single player
            pirates.rewardPenalty(gameManager.getGame().getBoard(), player);

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            sender.sendMessage(new PlayerMovedBackwardMessage(pirates.getPenaltyDays()));
            sender.sendMessage(new PlayerGetsCreditsMessage(pirates.getRewardCredits()));
            gameManager.broadcastGameMessage(new AnotherPlayerMovedBackwardMessage(player.getName(), pirates.getPenaltyDays()));
            gameManager.broadcastGameMessage(new AnotherPlayerGetsCreditsMessage(player.getName(), pirates.getRewardCredits()));

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

            phase = "HANDLE_DEFEATED_PLAYERS";
            handleDefeatedPlayers();
        }
    }

    /**
     * Handles defeated players
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void handleDefeatedPlayers() throws RemoteException {
        if (phase.equals("HANDLE_DEFEATED_PLAYERS")) {

            if (!defeatedPlayers.isEmpty() && !penaltyShots.isEmpty()) {

                // Sends to each defeated player information about incoming shot
                for (Player defeatedPlayer : defeatedPlayers) {
                    Sender sender = gameManager.getSenderByPlayer(defeatedPlayer);

                    sender.sendMessage(new IncomingProjectileMessage(penaltyShots.getFirst().getSize(), penaltyShots.getFirst().getFrom()));
                }

                phase = "ASK_ROLL_DICE";
                askToRollDice();

            } else {

                phase = "END";
                end();
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
        if (phase.equals("ASK_ROLL_DICE")) {

            // Asks first defeated player to roll dice
            Sender sender = gameManager.getSenderByPlayer(defeatedPlayers.getFirst());

            if (penaltyShots.getFirst().getFrom() == 0 || penaltyShots.getFirst().getFrom() == 2) {
                sender.sendMessage("ThrowDiceToFindColumn");

            } else if (penaltyShots.getFirst().getFrom() == 1 || penaltyShots.getFirst().getFrom() == 3) {
                sender.sendMessage("ThrowDiceToFindRow");
            }

            phase = "ROLL_DICE";
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
    public void rollDice(Player player, Sender sender) throws RemoteException {
        if (phase.equals("ROLL_DICE")) {

            // Checks if the player that calls the methods is also the first defeated player
            if (player.equals(defeatedPlayers.getFirst())) {

                diceResult = player.rollDice();

                sender.sendMessage(new DiceResultMessage(diceResult));
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiceResultMessage(defeatedPlayers.getFirst().getName(), diceResult), sender);

                if (penaltyShots.getFirst().getSize().equals(ProjectileSize.SMALL)) {
                    phase = "ASK_SHIELDS";
                    askToUseShields();

                } else {
                    phase = "HANDLE_SHOT";
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
    private void askToUseShields() throws RemoteException {
        if (phase.equals("ASK_SHIELDS")) {

            for (Player defeatedPlayer : defeatedPlayers) {

                // Checks if current player has a shield that covers that direction
                boolean hasShield = pirates.checkShields(defeatedPlayer, penaltyShots.getFirst());

                // Asks current defeated player if he wants to use a shield
                Sender sender = gameManager.getSenderByPlayer(defeatedPlayer);

                if (hasShield && defeatedPlayer.getSpaceship().getBatteriesCount() > 0) {
                    sender.sendMessage("AskToUseShield");

                } else {
                    shieldNotProtectedPlayers.add(defeatedPlayer);
                    sender.sendMessage("NoShieldAvailable");
                }
            }

            if (shieldNotProtectedPlayers.size() == defeatedPlayers.size()) {
                phase = "HANDLE_SHOT";
                handleShot();

            } else {
                phase = "SHIELD_DECISION";
            }
        }
    }

    public synchronized void receiveShieldDecision(Player player, String response, Sender sender) throws RemoteException {
        if (phase.equals("SHIELD_DECISION")) {

            // Checks if it is not part of non-protected player, and it is not already contained in protected one list
            if (!shieldNotProtectedPlayers.contains(player) && !shieldProtectedPlayers.contains(player)) {

                String upperCaseResponse = response.toUpperCase();

                switch (upperCaseResponse) {
                    case "YES":
                        shieldProtectedPlayers.add(player);
                        discardedBatteryForShield.add(player);
                        break;

                    case "NO":
                        shieldNotProtectedPlayers.add(player);
                        break;

                    default:
                        sender.sendMessage("IncorrectResponse");
                        break;
                }

                // Checks that every player has given his preference
                if (shieldNotProtectedPlayers.size() + shieldProtectedPlayers.size() == defeatedPlayers.size()) {

                    if (!shieldProtectedPlayers.isEmpty()) {

                        // Asks for a battery to each protected player
                        for (Player protectedPlayer : shieldProtectedPlayers) {
                            Sender senderProtected = gameManager.getSenderByPlayer(protectedPlayer);

                            senderProtected.sendMessage(new BatteriesToDiscardMessage(1));
                        }

                        phase = "SHIELD_BATTERY";

                    } else {
                        phase = "HANDLE_SHOT";
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
    public synchronized void receiveShieldBattery(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException {
        if (phase.equals("SHIELD_BATTERY")) {

            // Checks if the player that calls the methods has to discard a battery to activate a shield
            if (discardedBatteryForShield.contains(player)) {

                Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
                Component batteryStorage = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

                if (batteryStorage != null && batteryStorage.getType().equals(ComponentType.BATTERY_STORAGE)) {

                    // Checks if a battery has been discarded
                    if (pirates.chooseDiscardedBattery(player.getSpaceship(), (BatteryStorage) batteryStorage)) {

                        discardedBatteryForShield.remove(player);
                        sender.sendMessage("BatteryDiscarded");

                        if (discardedBatteryForShield.isEmpty()) {
                            phase = "HANDLE_SHOT";
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
    private void handleShot() throws RemoteException {
        if (phase.equals("HANDLE_SHOT")) {

            Game game = gameManager.getGame();
            Projectile shot = penaltyShots.getFirst();

            // For each non-protected player handles penalty shot
            for (Player shieldNotProtectedPlayer : shieldNotProtectedPlayers) {
                Component destroyedComponent = pirates.penaltyShot(game, shieldNotProtectedPlayer, shot, diceResult);

                // Gets current player sender reference
                Sender sender = gameManager.getSenderByPlayer(shieldNotProtectedPlayer);

                // Sends two types of messages based on the shot's result
                if (destroyedComponent != null) {
                    // TODO: handle waiting in case of needed decision by player on which part of the ship to hold
                    SpaceshipController.destroyComponent(gameManager, shieldNotProtectedPlayer, destroyedComponent.getY(), destroyedComponent.getX(), sender);

                } else {
                    gameManager.broadcastGameMessage("NothingGotDestroyed");
                }
            }

            // Checks if someone lost
            for (Player shieldNotProtectedPlayer : shieldProtectedPlayers) {

                // Total amount of crew members
                int totalCrew = shieldNotProtectedPlayer.getSpaceship().getTotalCrewCount();

                if (totalCrew == 0) {
                    gameManager.broadcastGameMessage(new PlayerDefeatedMessage(shieldNotProtectedPlayer.getName()));
                    gameManager.getGame().getBoard().leaveTravel(shieldNotProtectedPlayer);

                    // Remove him from defeated players still to handle
                    defeatedPlayers.remove(shieldNotProtectedPlayer);
                }
            }

            // Resets elaboration attributes
            shieldProtectedPlayers.clear();
            shieldNotProtectedPlayers.clear();
            discardedBatteryForShield.clear();

            // Removes just handled shot
            penaltyShots.removeFirst();

            // Next shot
            phase = "HANDLE_DEFEATED_PLAYERS";
            handleDefeatedPlayers();
        }
    }

    /**
     * Send a message of end card to all players
     *
     * @author Gabriele
     * @throws RemoteException
     */
    private void end() throws RemoteException {
        if (phase.equals("END")) {
            gameManager.broadcastGameMessage("This event card is finished");
        }
    }
}