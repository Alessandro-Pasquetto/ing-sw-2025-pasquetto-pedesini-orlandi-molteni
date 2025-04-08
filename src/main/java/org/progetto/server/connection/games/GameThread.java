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

                        gameManager.getGame().setPhase(GamePhase.BUILDING);

                        break;

                    case BUILDING:
                        System.out.println("Building...");
                        GameController.startBuilding(gameManager);

                        gameManager.getGame().resetReadyPlayers();
                        synchronized (gameThreadLock) {
                            while (game.getNumReadyPlayers() != game.getMaxNumPlayers() && !gameManager.getTimerExpired())
                                gameThreadLock.wait();
                        }

                        if(gameManager.getTimerExpired()){
                            gameManager.broadcastGameToNotReadyPlayersMessage("TimerExpired");
                            System.out.println("TimerExpired");
                        }
                        gameManager.getTimerController().stopTimer();

                        // todo la riga sotto è da eliminare quando si farà che il ready in building fa anche il placeLastComponent
                        gameManager.broadcastGameMessage("TimerExpired");
                        // Waiting for placing the last component
                        waitPlayersReady(game);

                        if(!BuildingController.checkAllShipValidity(gameManager)){
                            System.out.println("Adjusting spaceships...");
                            // Waiting for adjusting spaceship (don't do another phase for this, bcs custom actions)
                            waitPlayersReady(game);
                        }

                        //todo preparing players in track?

                        System.out.println("End building phase...");
                        game.setPhase(GamePhase.EVENT);
                        break;

                    case EVENT:
                        if(gameManager.getGame().getEventDeckSize() == 0){
                            game.setPhase(GamePhase.ENDGAME);
                            break;
                        }

                        System.out.println();
                        System.out.println("New event...");

                        /*
                        //todo richiedere al player di pescare una carta
                        waitFirstNotify();
                         */

                        // todo da rimuovere, sarà chiamato da un player
                        EventController.pickEventCard(gameManager);

                        gameManager.createEventController();
                        gameManager.getEventController().start();

                        gameManager.getGame().getBoard().updateTurnOrder();
                        gameManager.getGame().setActiveEventCard(null);
                        gameManager.broadcastGameMessage("This event card is finished");
                        break;

                    case ENDGAME:
                        System.out.println("Endgame...");
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

    public void waitFirstNotify() throws InterruptedException {
        synchronized (gameThreadLock) {
            gameThreadLock.wait();
        }
    }

    /**
     * Pauses the game thread until the player is ready
     *
     * @author Alessandro
     */
    public void waitPlayerReady(Player player) throws InterruptedException {
        player.setIsReady(false, gameManager.getGame());

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
    public void waitPlayersReady(Game game) throws InterruptedException {
        gameManager.getGame().resetReadyPlayers();

        synchronized (gameThreadLock) {
            while (game.getNumReadyPlayers() != game.getMaxNumPlayers())
                gameThreadLock.wait();
        }
    }

    // todo?: resetTravelersReady
    public void waitTravelersReady(Game game) throws InterruptedException {
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