package org.progetto.server.connection.games;

import java.util.HashMap;

/**
 * Class to handle:
 * - waitingGamesManager: map of non started games
 * - allGamesManager: map of already started games
 */
public class GameCommunicationHandlerMaps {

    // =======================
    // ATTRIBUTES
    // =======================

    private static final HashMap<Integer, GameCommunicationHandler> waitingGamesManager = new HashMap<>();
    private static final HashMap<Integer, GameCommunicationHandler> allGamesManager = new HashMap<>();

    // =======================
    // GETTERS
    // =======================

    public static GameCommunicationHandler getWaitingGameManager(int idGame) {
        synchronized (waitingGamesManager) {
            return waitingGamesManager.get(idGame);
        }
    }

    public static GameCommunicationHandler getGameManager(int idGame) {
        synchronized (allGamesManager) {
            return allGamesManager.get(idGame);
        }
    }

    public static int getNumWaitingGames() {
        synchronized (waitingGamesManager) {
            return waitingGamesManager.size();
        }
    }

    // =======================
    // OTHER METHODS
    // =======================

    public static void addWaitingGameManager(int idGame, GameCommunicationHandler gameCommunicationHandler) {
        synchronized (waitingGamesManager) {
            waitingGamesManager.put(idGame, gameCommunicationHandler);
        }
        synchronized (allGamesManager) {
            allGamesManager.put(idGame, gameCommunicationHandler);
        }
    }

    public static void removeWaitingGameManager(int idGame) {
        synchronized (waitingGamesManager) {
            waitingGamesManager.remove(idGame);
        }
    }

    public static void removeGameManager(int idGame) {
        synchronized (waitingGamesManager) {
            waitingGamesManager.remove(idGame);
        }

        synchronized (allGamesManager) {
            allGamesManager.remove(idGame);
        }
    }
}
