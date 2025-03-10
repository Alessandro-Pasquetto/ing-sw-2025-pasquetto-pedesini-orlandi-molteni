package org.progetto.server.controller;

import java.util.ArrayList;

public class GameControllersQueue {
    // =======================
    // ATTRIBUTES
    // =======================

    private static final ArrayList<GameController> gameControllers = new ArrayList<>();

    // =======================
    // CONSTRUCTORS
    // =======================



    // =======================
    // GETTERS
    // =======================

    public static GameController getGameController(int i) {
        synchronized (gameControllers) {
            return gameControllers.get(i);
        }
    }

    public static int getNumWaitingGames() {
        synchronized (gameControllers) {
            return gameControllers.size();
        }
    }

    // =======================
    // SETTERS
    // =======================

    public static void addGameController(GameController gameController) {
        synchronized (gameControllers) {
            gameControllers.add(gameController);
        }
    }

    public static void removeGameController(GameController gameController) {
        synchronized (gameControllers) {
            gameControllers.remove(gameController);
        }
    }

    // =======================
    // OTHER METHODS
    // =======================


}
