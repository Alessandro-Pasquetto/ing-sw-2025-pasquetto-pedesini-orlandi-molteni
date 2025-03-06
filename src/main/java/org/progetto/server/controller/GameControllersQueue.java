package org.progetto.server.controller;

import org.progetto.server.model.Game;

import java.util.ArrayList;

public class GameControllersQueue {
    private static ArrayList<GameController> gameControllers = new ArrayList<>();

    public static void addGameController(GameController gameController) {
        synchronized (gameControllers) {
            gameControllers.add(gameController);
        }
    }

    public static void removeGameController(Game gameController) {
        synchronized (gameControllers) {
            gameControllers.remove(gameController);
        }
    }

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
}
