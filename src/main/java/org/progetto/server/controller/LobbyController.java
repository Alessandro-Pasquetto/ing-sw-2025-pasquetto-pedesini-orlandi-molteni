package org.progetto.server.controller;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameCommunicationHandler;
import org.progetto.server.connection.games.GameCommunicationHandlerMaps;
import org.progetto.server.connection.rmi.RmiServer;
import org.progetto.server.connection.socket.SocketServer;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.internalMessages.InternalGameInfo;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
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

    // Create game objects and player, add player to the game
    public static InternalGameInfo createGame(String name, int levelGame, int numPlayers){
        int idGame = currentIdGame.getAndIncrement();

        GameCommunicationHandler gameCommunicationHandler = new GameCommunicationHandler(idGame, numPlayers, levelGame);
        Player player = new Player(name, 0, levelGame);

        gameCommunicationHandler.getGame().addPlayer(player);

        return new InternalGameInfo(gameCommunicationHandler, player);
    }

    public static InternalGameInfo joinGame(int idGame ,String name) throws IllegalStateException{

        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getWaitingGameManager(idGame);
        Game game = gameCommunicationHandler.getGame();

        if(!game.checkAvailableName(name))
            throw new IllegalStateException("NotAvailableName");

        Player player = new Player(name, game.getPlayersSize(), game.getLevel());

        gameCommunicationHandler.getGame().addPlayer(player);

        return new InternalGameInfo(gameCommunicationHandler, player);
    }
}