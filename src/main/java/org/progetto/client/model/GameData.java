package org.progetto.client.model;

import org.progetto.client.connection.Sender;

import java.io.File;
import java.io.IOException;

/**
 * Client data useful to track game evolution
 */
public class GameData {

    // =======================
    // ATTRIBUTES
    // =======================

    private static String clientId;
    private static File saveFile;

    private static Sender sender;
    private static int idGame;
    private static int levelGame;
    private static String phaseGame = "LOBBY";
    private static String namePlayer;
    private static int color;
    private static String UIType;

    // =======================
    // GETTERS
    // =======================

    public static String getClientId() {
        return clientId;
    }

    public static Sender getSender() {
        return sender;
    }

    public static int getIdGame() {
        return idGame;
    }

    public static int getLevelGame() {
        return levelGame;
    }

    public static String getPhaseGame() {
        return phaseGame;
    }

    public static String getNamePlayer(){
        return namePlayer;
    }

    public static String getUIType(){
        return UIType;
    }

    public static int getColor() {
        return color;
    }

    // =======================
    // SETTERS
    // =======================

    public static void setClientId(String clientId) {
        GameData.clientId = clientId;
    }

    public static void setSender(Sender sender) {
        GameData.sender = sender;
    }

    public static void setIdGame(int idGame) {
        GameData.idGame = idGame;
    }

    public static void setLevelGame(int levelGame) {
        GameData.levelGame = levelGame;
    }

    public static void setPhaseGame(String phaseGame) {
        GameData.phaseGame = phaseGame;
    }

    public static void setNamePlayer(String namePlayer){
        GameData.namePlayer = namePlayer;
    }

    public static void setUIType(String UIType){
        GameData.UIType = UIType;
    }

    public static void setColor(int color) {
        GameData.color = color;
    }

    // =======================
    // OTHER METHODS
    // =======================

    public static void createSaveFile() {

        File saveDir = new File("saves");
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        saveFile = new File("saves/" + clientId + ".save");

        try {
            saveFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}