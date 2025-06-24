package org.progetto.client.tui;

import org.progetto.client.connection.rmi.RmiClientReceiver;
import org.progetto.client.connection.rmi.RmiClientSender;
import org.progetto.client.connection.socket.SocketClient;
import org.progetto.client.connection.socket.SocketWriter;
import org.progetto.client.model.GameData;

import java.io.IOException;
import java.rmi.NotBoundException;


public class ConnectionsCommands {

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Shows the waiting games
     *
     * @author Alessandro
     */
    public static void showWaitingGames(){
        GameData.getSender().updateGameList();
    }

    /**
     * Enable to create a game, usage : CreateGame playerName Level maxPlayers
     *
     * @author Alessandro
     * @param commandParts are segments of the command
     */
    public static void createGame(String[] commandParts){

        if(commandParts[1].length() > 16){
            System.err.println("Your name needs to be at most 16 characters");
            return;
        }

        GameData.setNamePlayer(commandParts[1]);
        try {
            GameData.getSender().createGame(Integer.parseInt(commandParts[2]), Integer.parseInt(commandParts[3]));
        } catch (NumberFormatException e){
            System.err.println("You must insert a number!");
        }
    }

    /**
     * Enable to join an already present game, usage : Join gameID playerName
     *
     * @author Alessandro
     * @param commandParts are segments of the command
     */
    public static void joinGame(String[] commandParts){

        if(commandParts[2].length() > 16){
            System.err.println("Your name needs to be at most 16 characters");
            return;
        }

        GameData.setNamePlayer(commandParts[2]);

        try {
            GameData.getSender().tryJoinToGame(Integer.parseInt(commandParts[1]));
        } catch (NumberFormatException e){
            System.err.println("You must insert a number!");
        }
    }
}