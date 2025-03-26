package org.progetto.server.controller;

import java.util.ArrayList;

public class GameManagersQueue {
    // =======================
    // ATTRIBUTES
    // =======================

    private static final ArrayList<GameManager> gameManagers = new ArrayList<>();

    // =======================
    // GETTERS
    // =======================

    public static GameManager getGameManager(int i) {
        synchronized (gameManagers) {
            return gameManagers.get(i);
        }
    }

    public static int getNumWaitingGames() {
        synchronized (gameManagers) {
            return gameManagers.size();
        }
    }

    // =======================
    // OTHER METHODS
    // =======================

    public static void addGameManager(GameManager gameManager) {
        synchronized (gameManagers) {
            gameManagers.add(gameManager);
        }
    }

    public static void removeGameManager(GameManager gameManager) {
        synchronized (gameManagers) {
            gameManagers.remove(gameManager);
        }
    }
}
