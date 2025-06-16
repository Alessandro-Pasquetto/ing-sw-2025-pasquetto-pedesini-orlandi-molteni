package org.progetto.server.controller;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.*;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameManagerMaps;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lobby controller class
 */
public class LobbyController {

    // =======================
    // ATTRIBUTES
    // =======================

    private static final AtomicInteger currentIdGame = new AtomicInteger(0);
    private static final ArrayList<Sender> senders = new ArrayList<>();

    private static int lobbyDisconnectionDetectionInterval;

    // =======================
    // GETTERS
    // =======================

    public static ArrayList<Sender> getSendersCopy() {
        ArrayList<Sender> socketWritersCopy;

        synchronized (senders) {
            socketWritersCopy = new ArrayList<>(senders);
        }

        return socketWritersCopy;
    }

    // =======================
    // SETTERS
    // =======================

    public static void setLobbyDisconnectionDetectionInterval(int lobbyDisconnectionDetectionInterval) {
        LobbyController.lobbyDisconnectionDetectionInterval = lobbyDisconnectionDetectionInterval;
    }
    
    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Allows to start the ping method
     *
     * @author Alessandro
     */
    public static void startLobbyPinger(){

        Thread pingThread = new Thread(() -> {
            while (true) {
                try {
                    lobbyPinger();
                    Thread.sleep(lobbyDisconnectionDetectionInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        pingThread.setName("LobbyPingerThread");
        pingThread.setDaemon(true);
        pingThread.start();
    }


    /**
     * Pings for check connection with the lobby
     *
     * @author Alessandro
     */
    private static void lobbyPinger(){

        ArrayList<Sender> sendersCopy = getSendersCopy();

        for (Sender sender : sendersCopy) {
            if (sender instanceof VirtualClient vc) {
                try{
                    vc.ping();
                } catch (RemoteException e) {
                    removeSender(sender);
                }
            }
        }
    }

    /**
     * Add sender to the list
     *
     * @author Alessandro
     * @param sender
     */
    public static void addSender(Sender sender) {
        synchronized(currentIdGame){
            senders.add(sender);
        }
    }

    /**
     * Remove sender from the list
     *
     * @author Alessandro
     * @param sender
     */
    public static void removeSender(Sender sender) {
        synchronized(currentIdGame){
            senders.remove(sender);
        }
    }

    /**
     * Send message to all clients in lobby
     *
     * @author Alessandro
     * @param messageObj is the message to send
     */
    public synchronized static void broadcastLobbyMessage(Object messageObj) {

        ArrayList<Sender> sendersCopy = getSendersCopy();

        for (Sender sender : sendersCopy) {
            MessageSenderService.sendMessage(messageObj, sender);
        }
    }

    /**
     * Show all waiting games
     *
     * @author Gabriele
     * @param sender is the current Sender
     */
    public synchronized static void showWaitingGames(Sender sender) {

        ArrayList<GameManager> waitingGameManagers = new ArrayList<>(GameManagerMaps.getWaitingGamesMapCopy().values());
        ArrayList<WaitingGameInfoMessage> waitingGameInfoMessages = new ArrayList<>();

        for (GameManager gameManager : waitingGameManagers) {
            Game game = gameManager.getGame();

            WaitingGameInfoMessage waitingGameInfoMessage = new WaitingGameInfoMessage(game.getId(), game.getLevel(), game.getMaxNumPlayers(), game.getPlayersCopy());
            waitingGameInfoMessages.add(waitingGameInfoMessage);
        }

        MessageSenderService.sendMessage(new WaitingGamesMessage(waitingGameInfoMessages), sender);
    }

    /**
     * Create game objects and player, add player to the game
     *
     * @author Alessandro
     * @param name is the player's username
     * @param levelGame is the game level
     * @param numPlayers is the number of maximum game players
     * @return the GameManager of the created Game
     */
    public synchronized static GameManager createGame(String name, int levelGame, int numPlayers, Sender sender) throws IllegalStateException{
        int idGame = currentIdGame.getAndIncrement();

        if (numPlayers <= 1 || numPlayers > 4) {
            throw new IllegalStateException("NotValidPlayerNumber");
        }

        if (levelGame <= 0 || levelGame > 2) {
            throw new IllegalStateException("NotValidLevel");
        }

        GameManager gameManager = new GameManager(idGame, numPlayers, levelGame);
        Player player = new Player(name);

        Game game = gameManager.getGame();

        game.addPlayer(player);

        LobbyController.removeSender(sender);
        gameManager.addSender(player, sender);

        // Messages
        MessageSenderService.sendMessage(new GameInfoMessage(idGame, levelGame, numPlayers), sender);
        MessageSenderService.sendMessage(new WaitingPlayersMessage(game.getPlayersCopy()), sender);
        MessageSenderService.sendMessage(new NewGamePhaseMessage(game.getPhase().toString()), sender);

        GameManagerMaps.addWaitingGameManager(idGame, gameManager);
        broadcastLobbyMessage("UpdateGameList");

        return gameManager;
    }

    /**
     * Join game objects and player, add player to the game
     *
     * @author Alessandro
     * @param idGame is the game id of an existing Lobby
     * @param name is the player's username
     * @param sender is the current Sender
     * @return gameManager of the joined game
     */
    public static GameManager joinGame(int idGame, String name, Sender sender) throws IllegalStateException{

        GameManager gameManager = GameManagerMaps.getWaitingGameManager(idGame);

        if(gameManager == null)
            throw new IllegalStateException("NotValidGameId");

        Game game = gameManager.getGame();

        if(!game.checkAvailableName(name))
            throw new IllegalStateException("NotAvailableName");

        Player player = new Player(name);

        gameManager.getGame().addPlayer(player);

        LobbyController.removeSender(sender);
        gameManager.addSender(player, sender);

        if(game.getPlayersSize() == game.getMaxNumPlayers())
            GameManagerMaps.removeWaitingGameManager(game.getId());


        broadcastLobbyMessage("UpdateGameList");
        MessageSenderService.sendMessage(new GameInfoMessage(idGame, game.getLevel(), game.getMaxNumPlayers()), sender);
        gameManager.broadcastGameMessage(new WaitingPlayersMessage(game.getPlayersCopy()));

        gameManager.getGameThread().notifyThread();

        return gameManager;
    }

    /**
     * Allows a player to reconnect to an existing game
     *
     * @param idGame is the game id of an existing Game
     * @param name is the player's username
     * @param sender is the current Sender
     * @return the GameManager of the game in which the player has been reconnected
     * @throws IllegalStateException if it was impossible to reconnect to the game
     */
    public static GameManager reconnectToGame(int idGame, String name, Sender sender) throws IllegalStateException{

        GameManager gameManager = GameManagerMaps.getGameManager(idGame);

        if(gameManager == null)
            throw new IllegalStateException("FailedToReconnect");

        gameManager.reconnectPlayer(name, sender);

        return gameManager;
    }
}