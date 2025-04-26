package org.progetto.client.tui;

import org.progetto.client.connection.rmi.RmiClientReceiver;
import org.progetto.client.connection.rmi.RmiClientSender;
import org.progetto.client.connection.socket.SocketClient;
import org.progetto.client.connection.socket.SocketWriter;
import org.progetto.client.model.GameData;

/**
 * Contains commands relating to the lobby phase
 */
public class ConnectionsCommands {

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Enable connection to a game, usage : Connect IP port
     *
     * @author Alessandro
     * @param commandParts are segments of the command
     */
    public static void connect(String[] commandParts){

        String ip = commandParts[1];
        int port = Integer.parseInt(commandParts[2]);

        GameData.getSender().connect(ip, port);
    }

    /**
     * Shows the waiting games
     *
     * @author Alessandro
     */
    public static void showWaitingGames(){
        GameData.getSender().updateGameList();
    }

    /**
     * Enable to create a game, usage : CreateGame playerName
     *
     * @author Alessandro
     * @param commandParts are segments of the command
     */
    public static void createGame(String[] commandParts){

        GameData.setNamePlayer(commandParts[1]);
        GameData.getSender().createGame(Integer.parseInt(commandParts[2]), Integer.parseInt(commandParts[3]));
    }

    /**
     * Enable to join an already present game, usage : Join gameID playerName
     *
     * @author Alessandro
     * @param commandParts are segments of the command
     */
    public static void joinGame(String[] commandParts){

        GameData.setNamePlayer(commandParts[2]);
        GameData.getSender().tryJoinToGame(Integer.parseInt(commandParts[1]));
    }

    /**
     * Allows the users to connect and automatically create a game
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     * @throws InterruptedException
     */
    public static void autoCreate(String[] commandParts) throws InterruptedException {

        if(GameData.getSender() instanceof RmiClientSender)
            connect(new String[]{"connect", "127.0.0.1", "1099"});
        else if (GameData.getSender() instanceof SocketClient)
            connect(new String[]{"connect", "127.0.0.1", "8080"});

        Thread.sleep(500);

        createGame(new String[]{"createGame", "player_1", "2", "2"});

        Thread.sleep(500);

        BuildingCommands.readyPlayer(null);
    }

    /**
     * Allows the users to connect and automatically create a game
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     * @throws InterruptedException
     */
    public static void autoJoin(String[] commandParts) throws InterruptedException {

        if(GameData.getSender() instanceof RmiClientSender)
            connect(new String[]{"connect", "127.0.0.1", "1099"});
        else if (GameData.getSender() instanceof SocketClient)
            connect(new String[]{"connect", "127.0.0.1", "8080"});

        Thread.sleep(500);

        joinGame(new String[]{"joinGame", commandParts[1], "player_" + Integer.toString((int) System.currentTimeMillis()/1000).substring(0, 2)});

        Thread.sleep(500);

        BuildingCommands.readyPlayer(null);
    }
}