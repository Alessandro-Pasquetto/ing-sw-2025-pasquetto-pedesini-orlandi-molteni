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
    private static int levelGame;
    private static String namePlayer;
    private static String UIType;

    // =======================
    // GETTERS
    // =======================

    public static Sender getSender() {
        return sender;
    }

    public static int getIdGame() {
        return idGame;
    }

    public static int getLevelGame() {
        return levelGame;
    }

    public static String getNamePlayer(){
        return namePlayer;
    }

    public static String getUIType(){
        return UIType;
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

    public static void setLevelGame(int levelGame) {
        GameData.levelGame = levelGame;
    }

    public static void setNamePlayer(String namePlayer){
        GameData.namePlayer = namePlayer;
    }

    public static void setUIType(String UIType){
        GameData.UIType = UIType;
    }
}