package org.progetto.server.controller;

import org.progetto.messages.toClient.ShowWaitingGamesMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameManagerMaps;
import org.progetto.server.connection.games.WaitingGameInfo;
import org.progetto.server.internalMessages.InternalGameInfo;
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

    // =======================
    // OTHER METHODS
    // =======================

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
    public static void broadcastLobbyMessage(Object messageObj) throws RemoteException {

        ArrayList<Sender> sendersCopy;

        synchronized (senders) {
            sendersCopy = new ArrayList<>(senders);
        }

        for (Sender sender : sendersCopy) {
            sender.sendMessage(messageObj);
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
    public static void broadcastLobbyMessageToOthers(Object messageObj, Sender sender) throws RemoteException {

        ArrayList<Sender> sendersCopy;

        synchronized (senders) {
            sendersCopy = new ArrayList<>(senders);
        }

        for (Sender s : sendersCopy) {
            if(!s.equals(sender)) {
                s.sendMessage(messageObj);
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
    public static void showWaitingGames(Sender sender) throws RemoteException {

        ArrayList<Integer> gameIds = new ArrayList<>(GameManagerMaps.getWaitingGamesMap().keySet());
        ArrayList<WaitingGameInfo> waitingGameInfos = new ArrayList<>();

        for (Integer gameId : gameIds) {
            Game game = GameManagerMaps.getGameManager(gameId).getGame();

            WaitingGameInfo waitingGameInfo = new WaitingGameInfo(gameId, game.getLevel(), game.getMaxNumPlayers(), game.getPlayersCopy());
            waitingGameInfos.add(waitingGameInfo);
        }

        sender.sendMessage(new ShowWaitingGamesMessage(waitingGameInfos));
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
    public static InternalGameInfo createGame(String name, int levelGame, int numPlayers) throws IllegalStateException {
        int idGame = currentIdGame.getAndIncrement();

        if (numPlayers <= 0 || numPlayers > 4) {
            throw new IllegalStateException("NotValidPlayerNumber");
        }

        if (levelGame <= 0 || levelGame > 2) {
            throw new IllegalStateException("NotValidLevel");
        }

        GameManager gameManager = new GameManager(idGame, numPlayers, levelGame);
        Player player = new Player(name, 0, levelGame);

        gameManager.getGame().addPlayer(player);
        gameManager.getGameThread().notifyThread();// todo se non può esserci un game con max 1 persona da togliere

        return new InternalGameInfo(gameManager, player);
    }

    /**
     * Join game objects and player, add player to the game
     *
     * @author Alessandro
     * @param idGame
     * @param name
     * @return
     */
    public static InternalGameInfo joinGame(int idGame, String name) throws IllegalStateException {

        GameManager gameManager = GameManagerMaps.getWaitingGameManager(idGame);

        if(gameManager == null){
            throw new IllegalStateException("NotValidGameId");
        }

        Game game = gameManager.getGame();

        if(!game.checkAvailableName(name))
            throw new IllegalStateException("NotAvailableName");

        Player player = new Player(name, game.getPlayersSize(), game.getLevel());

        gameManager.getGame().addPlayer(player);
        gameManager.getGameThread().notifyThread();

        return new InternalGameInfo(gameManager, player);
    }
}