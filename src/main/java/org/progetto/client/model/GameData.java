package org.progetto.client.model;

import org.progetto.client.connection.Sender;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.events.CardType;
import org.progetto.server.model.events.EventCard;

import java.io.*;
import java.util.ArrayList;

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
    private static EventCard activeCard;
    private static String namePlayer;
    private static String activePlayer;
    private static int color;
    private static Component[][] spaceship;
    private static Player[] track;
    private static ArrayList<Player> travelers;
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

    public static String getActivePlayer() {
        return activePlayer;
    }

    public static Component[][] getSpaceship() {
        return spaceship;
    }

    public static Player[] getTrack() {
        return track;
    }

    public static ArrayList<Player> getTravelers() {
        if (travelers == null) {
            travelers = new ArrayList<>();
        }
        return travelers;
    }

    public static String getUIType(){
        return UIType;
    }

    public static int getColor() {
        return color;
    }

    public static EventCard getActiveCard() {
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

    public static void setActivePlayer(String activePlayer) {
        GameData.activePlayer = activePlayer;
    }

    public static void setSpaceship(Component[][] spaceship) {
        GameData.spaceship = spaceship;
    }

    public static void setTrack(Player[] track) {
        GameData.track = track;
    }

    public static void setTravelers(ArrayList<Player> travelers) {
        GameData.travelers = travelers;
    }

    public static void setUIType(String UIType){
        GameData.UIType = UIType;
    }

    public static void setColor(int color) {
        GameData.color = color;
    }

    public static void setActiveCard(EventCard activeCard) {
        GameData.activeCard = activeCard;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Creates a save file for the current client
     * The file is created in the "saves" directory with the name "<clientId>.save"
     *
     * @author Alessandro
     */
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

    /**
     * Saves the game data to the save file
     * The data saved includes the game ID and player name
     *
     * @author Alessandro
     */
    public static void saveGameData() {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(saveFile))) {
            dos.writeInt(idGame);
            dos.writeUTF(namePlayer);

        } catch (IOException e) {
            System.err.println("Error saving data");
        }
    }

    /**
     * Restores the game data from the save file
     * The data restored includes the game ID and player name
     *
     * @author Alessandro
     */
    public static void restoreSavedGameData(){
        try (DataInputStream dis = new DataInputStream(new FileInputStream(saveFile))) {
            idGame = dis.readInt();
            namePlayer = dis.readUTF();

        } catch (IOException e) {
            System.err.println("Error reading data");
        }
    }

    /**
     * Clears the save file by writing an empty string to it
     * This method also resets the game ID and player name
     *
     * @author Alessandro
     */
    public static void clearSaveFile() {
        if (saveFile != null && saveFile.exists()) {
            try (FileWriter writer = new FileWriter(saveFile, false)) {
                writer.write("");

                idGame = 0;
                namePlayer = null;
            } catch (IOException e) {
                throw new RuntimeException("Error while clearing the save file", e);
            }
        }
    }

    /**
     * Calculate the modulus
     *
     * @author Alessandro
     * @param a the dividend
     * @param b the divisor
     * @return the modulus
     */
    private static int modulus(int a, int b) {
        int result = a % b;
        return (result < 0) ? result + b : result;
    }

    /**
     * Moves the player forward/backward on the track, identified by name
     *
     * @author Gabriele
     * @param playerName the name of the player to move
     * @param distance the number of steps to move (can be negative)
     * @throws IllegalArgumentException if player with the given name is not found
     */
    public static synchronized void movePlayerByDistance(String playerName, int distance) {
        Player[] track = GameData.getTrack();
        int currentPosition = -1;

        // Find the player's current position by name
        for (int i = 0; i < track.length; i++) {
            if (track[i] != null && playerName.equals(track[i].getName())) {
                currentPosition = i;
                break;
            }
        }

        // If the player is not found
        if (currentPosition == -1) {
            return;
        }

        Player player = track[currentPosition];
        track[currentPosition] = null;  // Removes player from current cell

        int sign = (distance < 0) ? -1 : 1;
        distance = Math.abs(distance);
        int position = currentPosition;

        // Moves step by step, skipping occupied cells
        while (distance != 0) {
            position += sign;
            int wrapped = modulus(position, track.length);

            if (track[wrapped] == null) {
                distance--;
            }
        }

        int finalPos = modulus(position, track.length);
        track[finalPos] = player;
    }
}