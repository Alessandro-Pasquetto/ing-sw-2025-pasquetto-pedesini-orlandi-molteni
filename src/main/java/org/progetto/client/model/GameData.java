package org.progetto.client.model;

import org.progetto.client.connection.Sender;

/**
 * Client data useful to track game evolution
 */
public class GameData {

    // =======================
    // ATTRIBUTES
    // =======================

    // Client data that I want to store
    private static Sender sender;
    private static int idGame;
    private static String namePlayer;

    // =======================
    // GETTERS
    // =======================

    public static Sender getSender() {
        return sender;
    }

    public static int getIdGame() {
        return idGame;
    }

    public static String getNamePlayer(){
        return namePlayer;
    }

    // =======================
    // SETTERS
    // =======================

    public static void setSender(Sender sender) {
        GameData.sender = sender;
    }

    public static void setIdGame(int idGame) {
        GameData.idGame = idGame;
    }

    public static void setNamePlayer(String namePlayer){
        GameData.namePlayer = namePlayer;
    }
}