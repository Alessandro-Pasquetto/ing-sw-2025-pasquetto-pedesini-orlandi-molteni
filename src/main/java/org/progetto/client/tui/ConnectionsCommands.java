package org.progetto.client.tui;

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
        GameData.getSender().showWaitingGames();
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
}