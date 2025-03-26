package org.progetto.server.controller;

import org.progetto.client.connection.rmi.VirtualView;
import org.progetto.server.connection.rmi.RmiServer;
import org.progetto.server.connection.socket.SocketServer;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.internalMessages.InternalGameInfo;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import java.util.concurrent.atomic.AtomicInteger;

public class LobbyController {

    private static final AtomicInteger currentIdGame = new AtomicInteger(0);

    // Send message to all clients in lobby
    public static void broadcastLobbyMessage(Object messageObj) {
        SocketServer.broadcastLobbyMessage(messageObj);
        RmiServer.broadcastMessage(messageObj);
    }

    public static void broadcastLobbyMessageToOthers(Object messageObj, SocketWriter swSender, VirtualView vvSender) {
        SocketServer.broadcastLobbyMessageToOthers(swSender, messageObj);
        RmiServer.broadcastMessageToOthers(vvSender, messageObj);
    }

    // Create game objects and player, add player to the game
    public static InternalGameInfo createGame(String name, int levelGame, int numPlayers){
        int idGame = currentIdGame.getAndIncrement();

        GameManager gameManager = new GameManager(idGame, numPlayers, levelGame);
        Player player = new Player(name, 0, levelGame);

        gameManager.getGame().addPlayer(player);

        return new InternalGameInfo(gameManager, player);
    }

    public static InternalGameInfo joinGame(int idGame ,String name) throws IllegalStateException{

        GameManager gameManager = GameManagersQueue.getGameManager(idGame);
        Game game = gameManager.getGame();

        if(!game.checkAvailableName(name))
            throw new IllegalStateException("NotAvailableName");

        Player player = new Player(name, game.getPlayersSize(), game.getLevel());

        gameManager.getGame().addPlayer(player);

        return new InternalGameInfo(gameManager, player);
    }
}