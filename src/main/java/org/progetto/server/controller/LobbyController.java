package org.progetto.server.controller;

import org.progetto.messages.toClient.ShowWaitingGamesMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameManagerMaps;
import org.progetto.server.connection.games.WaitingGameInfo;
import org.progetto.server.connection.rmi.RmiServer;
import org.progetto.server.connection.socket.SocketServer;
import org.progetto.server.connection.socket.SocketWriter;
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

    // =======================
    // OTHER METHODS
    // =======================

    // Send message to all clients in lobby
    public static void broadcastLobbyMessage(Object messageObj) {
        SocketServer.broadcastLobbyMessage(messageObj);
        RmiServer.broadcastLobbyMessage(messageObj);
    }

    public static void broadcastLobbyMessageToOthers(Object messageObj, Sender sender) {

        if(sender instanceof SocketWriter){
            SocketServer.broadcastLobbyMessageToOthers(sender, messageObj);
            RmiServer.broadcastLobbyMessage(messageObj);
        }else{
            RmiServer.broadcastLobbyMessageToOthers(sender, messageObj);
            SocketServer.broadcastLobbyMessage(messageObj);
        }
    }

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

    // Create game objects and player, add player to the game
    public static InternalGameInfo createGame(String name, int levelGame, int numPlayers){
        int idGame = currentIdGame.getAndIncrement();

        GameManager gameManager = new GameManager(idGame, numPlayers, levelGame);
        Player player = new Player(name, 0, levelGame);

        gameManager.getGame().addPlayer(player);

        return new InternalGameInfo(gameManager, player);
    }

    public static InternalGameInfo joinGame(int idGame, String name) throws IllegalStateException{

        GameManager gameManager = GameManagerMaps.getWaitingGameManager(idGame);

        if(gameManager == null){
            throw new IllegalStateException("Not valid gameId");
        }

        Game game = gameManager.getGame();

        if(!game.checkAvailableName(name))
            throw new IllegalStateException("NotAvailableName");

        Player player = new Player(name, game.getPlayersSize(), game.getLevel());

        gameManager.getGame().addPlayer(player);

        return new InternalGameInfo(gameManager, player);
    }
}