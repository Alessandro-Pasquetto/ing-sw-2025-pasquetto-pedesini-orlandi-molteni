package org.progetto.client.model;

import org.progetto.client.connection.Sender;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.events.CardType;

import java.io.*;

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
    private static CardType activeCard;
    private static String namePlayer;
    private static int color;
    private static String UIType;

    // =======================
    // GETTERS
    // =======================

    public static String getClientId() {
        return clientId;
    }

    public static boolean hasSavedGameData(){
        return saveFile.length() != 0;
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

    public static CardType getActiveCard() {
        return activeCard;
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

    public static void setActiveCard(CardType activeCard) {
        GameData.activeCard = activeCard;
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

    public static void saveGameData() {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(saveFile))) {
            dos.writeInt(idGame);
            dos.writeUTF(namePlayer);

        } catch (IOException e) {
            System.err.println("Error saving data");
        }
    }

    public static void restoreSavedGameData(){
        try (DataInputStream dis = new DataInputStream(new FileInputStream(saveFile))) {
            idGame = dis.readInt();
            namePlayer = dis.readUTF();

        } catch (IOException e) {
            System.err.println("Error reading data");
        }
    }

    public static void clearSaveFile() {
        if (saveFile != null && saveFile.exists()) {
            try (FileWriter writer = new FileWriter(saveFile, false)) {
                writer.write("");
            } catch (IOException e) {
                throw new RuntimeException("Errore durante la pulizia del file di salvataggio.", e);
            }
        }
    }
}