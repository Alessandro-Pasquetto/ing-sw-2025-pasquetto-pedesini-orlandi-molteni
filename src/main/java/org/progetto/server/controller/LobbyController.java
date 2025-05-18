package org.progetto.server.controller;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.*;
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

    public static void startLobbyPinger(){

        Thread pingThread = new Thread(() -> {
            while (true) {
                try {
                    lobbyPinger();
                    Thread.sleep(lobbyDisconnectionDetectionInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        pingThread.setDaemon(true);
        pingThread.start();
    }

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
     * @param messageObj
     * @throws RemoteException
     */
    public synchronized static void broadcastLobbyMessage(Object messageObj) {

        ArrayList<Sender> sendersCopy = getSendersCopy();

        for (Sender sender : sendersCopy) {
            try {
                sender.sendMessage(messageObj);
            } catch (RemoteException e) {
                System.err.println("RMI client unreachable");
            }
        }
    }

    /**
     * Send message to all clients in lobby except the sender
     *
     * @author Alessandro
     * @param messageObj
     * @param sender
     * @throws RemoteException
     */
    public synchronized static void broadcastLobbyMessageToOthers(Object messageObj, Sender sender) {

        ArrayList<Sender> sendersCopy = getSendersCopy();

        for (Sender s : sendersCopy) {
            if(!s.equals(sender)) {
                try {
                    s.sendMessage(messageObj);
                } catch (RemoteException e) {
                    System.err.println("RMI client unreachable");
                }
            }
        }
    }

    /**
     * Show all waiting games
     *
     * @author Gabriele
     * @param sender
     * @throws RemoteException
     */
    public synchronized static void showWaitingGames(Sender sender) throws RemoteException {

        ArrayList<GameManager> waitingGameManagers = new ArrayList<>(GameManagerMaps.getWaitingGamesMapCopy().values());
        ArrayList<WaitingGameInfoMessage> waitingGameInfoMessages = new ArrayList<>();

        for (GameManager gameManager : waitingGameManagers) {
            Game game = gameManager.getGame();

            WaitingGameInfoMessage waitingGameInfoMessage = new WaitingGameInfoMessage(game.getId(), game.getLevel(), game.getMaxNumPlayers(), game.getPlayersCopy());
            waitingGameInfoMessages.add(waitingGameInfoMessage);
        }

        sender.sendMessage(new WaitingGamesMessage(waitingGameInfoMessages));
    }

    /**
     * Create game objects and player, add player to the game
     *
     * @author Alessandro
     * @param name
     * @param levelGame
     * @param numPlayers
     * @return
     */
    public synchronized static GameManager createGame(String name, int levelGame, int numPlayers, Sender sender) throws IllegalStateException, RemoteException {
        int idGame = currentIdGame.getAndIncrement();

        if (numPlayers <= 0 || numPlayers > 4) {
            throw new IllegalStateException("NotValidPlayerNumber");
        }

        if (levelGame <= 0 || levelGame > 2) {
            throw new IllegalStateException("NotValidLevel");
        }

        GameManager gameManager = new GameManager(idGame, numPlayers, levelGame);
        Player player = new Player(name, 0, levelGame);

        Game game = gameManager.getGame();

        game.addPlayer(player);

        LobbyController.removeSender(sender);
        gameManager.addSender(player, sender);

        // Messages
        sender.sendMessage(new GameInfoMessage(idGame, levelGame, numPlayers));
        sender.sendMessage(new WaitingPlayersMessage(game.getPlayersCopy()));
        sender.sendMessage(new NewGamePhaseMessage(game.getPhase().toString()));

        if(numPlayers != 1){
            GameManagerMaps.addWaitingGameManager(idGame, gameManager);
            broadcastLobbyMessage("UpdateGameList");
        }
        else
            gameManager.getGameThread().notifyThread();

        return gameManager;
    }

    /**
     * Join game objects and player, add player to the game
     *
     * @author Alessandro
     * @param idGame
     * @param name
     * @return
     */
    public static GameManager joinGame(int idGame, String name, Sender sender) throws IllegalStateException, RemoteException {

        GameManager gameManager = GameManagerMaps.getWaitingGameManager(idGame);

        if(gameManager == null)
            throw new IllegalStateException("NotValidGameId");

        Game game = gameManager.getGame();

        if(!game.checkAvailableName(name))
            throw new IllegalStateException("NotAvailableName");

        Player player = new Player(name, game.getPlayersSize(), game.getLevel());

        gameManager.getGame().addPlayer(player);

        LobbyController.removeSender(sender);
        gameManager.addSender(player, sender);

        broadcastLobbyMessage("UpdateGameList");
        sender.sendMessage(new GameInfoMessage(idGame, game.getLevel(), game.getMaxNumPlayers()));
        gameManager.broadcastGameMessage(new WaitingPlayersMessage(game.getPlayersCopy()));

        gameManager.getGameThread().notifyThread();

        return gameManager;
    }


    public static GameManager reconnectToGame(int idGame, String name, Sender sender) throws IllegalStateException{

        GameManager gameManager = GameManagerMaps.getGameManager(idGame);

        gameManager.reconnectPlayer(name, sender);

        return gameManager;
    }
}