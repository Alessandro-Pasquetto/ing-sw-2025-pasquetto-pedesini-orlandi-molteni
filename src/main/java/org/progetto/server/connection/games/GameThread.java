package org.progetto.server.connection.games;

import org.progetto.server.controller.BuildingController;
import org.progetto.server.controller.EventController;
import org.progetto.server.controller.GameController;
import org.progetto.server.model.Game;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;

import java.rmi.RemoteException;

public class GameThread extends Thread {

    // =======================
    // ATTRIBUTES
    // =======================

    GameManager gameManager;
    private boolean isRunning = true;
    private final Object gameThreadLock = new Object();

    // =======================
    // CONSTRUCTORS
    // =======================

    public GameThread(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    // =======================
    // RUN
    // =======================

    /**
     * Manages the game phases
     *
     * @author Alessandro
     */
    @Override
    public void run() {
        try {
            Game game = gameManager.getGame();

            while (isRunning) {
                switch (gameManager.getGame().getPhase()) {

                    case INIT:
                        System.out.println("Waiting players...");

                        waitPlayersReady(game);

                        GameController.startBuilding(gameManager);
                        break;

                    case BUILDING:
                        System.out.println("Building...");

                        synchronized (gameThreadLock) {
                            while (game.getNumReadyPlayers() != game.getMaxNumPlayers() && !gameManager.getTimerExpired())
                                gameThreadLock.wait();
                        }

                        gameManager.getTimerController().stopTimer();
                        gameManager.broadcastGameMessage("TimerExpired");
                        System.out.println("TimerExpired");

                        // Waiting for placing the last component
                        waitPlayersReady(game);

                        BuildingController.checkAllShipValidity(gameManager);

                        System.out.println("Adjusting spaceships...");
                        // Waiting for adjusting spaceship (don't do another phase for this, bcs custom actions)
                        waitPlayersReady(game);

                        //todo preparing players in track?



                        game.setPhase(GamePhase.EVENT);
                        break;

                    case EVENT:
                        System.out.println("Event...");

                        EventController.pickEventCard(gameManager);

                        waitPlayersReady(game);
                        break;
                }
            }
        } catch (InterruptedException | RemoteException e) {
            e.printStackTrace();
        }
    }

    // =======================
    // OTHER METHODS
    // =======================


    /**
     * Pauses the game thread until the player is ready
     *
     * @author Alessandro
     */
    private void waitPlayerReady(Player player) throws InterruptedException {
        synchronized (gameThreadLock) {
            while (!player.getIsReady())
                gameThreadLock.wait();
        }
    }


    /**
     * Pauses the game thread until all players are ready to continue
     *
     * @author Alessandro
     */
    private void waitPlayersReady(Game game) throws InterruptedException {
        synchronized (gameThreadLock) {
            while (game.getNumReadyPlayers() != game.getMaxNumPlayers())
                gameThreadLock.wait();
        }
        gameManager.getGame().resetReadyPlayers();
    }

    // todo?: resetTravelersReady
    private void waitTravelersReady(Game game) throws InterruptedException {
        /*
        synchronized (gameThreadLock) {
            while (game.getNumReadyPlayers() != game.getMaxNumPlayers())
                gameThreadLock.wait();
        }
        gameManager.getGame().getBoard().;

         */
    }

    /**
     * Notifies the game thread
     *
     */
    public void notifyThread(){
        synchronized (gameThreadLock){
            gameThreadLock.notify();
        }
    }

    /**
     * Stops the game thread
     *
     */
    public void stopThread() {
        isRunning = false;
    }
}