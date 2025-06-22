package org.progetto.server.connection.games;

import java.util.ArrayList;
import java.util.HashMap;


public class GameManagerMaps {

    // =======================
    // ATTRIBUTES
    // =======================

    private static final HashMap<Integer, GameManager> waitingGamesManager = new HashMap<>();
    private static final HashMap<Integer, GameManager> allGamesManager = new HashMap<>();

    // =======================
    // GETTERS
    // =======================

    public static GameManager getWaitingGameManager(int idGame) {
        synchronized (waitingGamesManager) {
            return waitingGamesManager.get(idGame);
        }
    }

    public static GameManager getGameManager(int idGame) {
        synchronized (allGamesManager) {
            return allGamesManager.get(idGame);
        }
    }

    public static int getNumWaitingGames() {
        synchronized (waitingGamesManager) {
            return waitingGamesManager.size();
        }
    }

    public static ArrayList<Integer> getIdWaitingGamesCopy(){

        ArrayList<Integer> getIdWaitingGamesCopy;

        synchronized (waitingGamesManager) {
            getIdWaitingGamesCopy = new ArrayList<>(waitingGamesManager.keySet());
        }
        return getIdWaitingGamesCopy;
    }

    public static HashMap<Integer, GameManager> getWaitingGamesMapCopy() {
        synchronized (waitingGamesManager) {
            return new HashMap<>(waitingGamesManager);
        }
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Adds a GameManager to the waitingGamesManager and allGamesManager if it doesn't already exist
     *
     * @author Alessandro
     * @param idGame the ID of the game
     * @param gameManager  the GameManager instance to add
     */
    public static void addWaitingGameManager(int idGame, GameManager gameManager) {
        synchronized (waitingGamesManager) {
            waitingGamesManager.put(idGame, gameManager);
        }
        synchronized (allGamesManager) {
            if (!allGamesManager.containsKey(idGame)) {
                allGamesManager.put(idGame, gameManager);
            }
        }
    }

    /**
     * Removes a GameManager from the waitingGamesManager
     *
     * @author Alessandro
     * @param idGame the ID of the game
     */
    public static void removeWaitingGameManager(int idGame) {
        synchronized (waitingGamesManager) {
            waitingGamesManager.remove(idGame);
        }
    }

    /**
     * Removes a GameManager from both waitingGamesManager and allGamesManager
     *
     * @author Alessandro
     * @param idGame the ID of the game
     */
    public static void removeGameManager(int idGame) {
        synchronized (waitingGamesManager) {
            waitingGamesManager.remove(idGame);
        }

        synchronized (allGamesManager) {
            allGamesManager.remove(idGame);
        }
    }
}
