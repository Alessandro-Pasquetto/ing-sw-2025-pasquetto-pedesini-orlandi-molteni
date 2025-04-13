package org.progetto.server.connection.games;

import org.progetto.server.controller.BuildingController;
import org.progetto.server.controller.EventController;
import org.progetto.server.controller.GameController;
import org.progetto.server.model.Board;
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
                        System.out.println("Waiting ready players...");

                        resetAndWaitPlayersReady();

                        gameManager.getGame().setPhase(GamePhase.BUILDING);
                        break;

                    case BUILDING:
                        System.out.println("Start building...");
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

                        // Waiting for placing the last component
                        waitPlayersReady();

                        if(!BuildingController.checkAllShipValidity(gameManager)){
                            System.out.println("Adjusting spaceships...");
                            // Waiting for adjusting spaceship (don't do another phase for this, bcs custom actions)
                            waitPlayersReady();
                        }

                        // preparing players in track
                        game.getBoard().addTravelersInTrack(game.getLevel());

                        System.out.println("End building phase...");
                        game.setPhase(GamePhase.EVENT);
                        break;

                    case EVENT:
                        System.out.println();
                        System.out.println("New event...");

                        EventController.pickEventCard(gameManager);

                        System.out.println(gameManager.getGame().getActiveEventCard().getType().toString());

                        gameManager.createEventController();
                        gameManager.getEventController().start();

                        gameManager.getGame().getBoard().updateTurnOrder();

                        EventController.handleDefeatedPlayers(gameManager);

                        gameManager.getGame().setActiveEventCard(null);
                        gameManager.broadcastGameMessage("This event card is finished");
                        game.setPhase(GamePhase.TRAVEL);
                        break;

                    case TRAVEL:
                        if(gameManager.getGame().getEventDeckSize() > 0){
                            gameManager.broadcastGameMessage("Do you want to continue traveling?");
                            resetAndWaitPlayersReady();
                            game.setPhase(GamePhase.EVENT);
                        }
                        else
                            game.setPhase(GamePhase.ENDGAME);
                        break;

                    case ENDGAME:
                        System.out.println();
                        System.out.println("Endgame...");
                        return;
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
     * Reset players ready and pauses the game thread until all players are ready to continue
     *
     * @author Alessandro
     */
    public void resetAndWaitPlayersReady() throws InterruptedException {
        Game game = gameManager.getGame();
        game.resetReadyPlayers();

        synchronized (gameThreadLock) {
            while (game.getNumReadyPlayers() != game.getMaxNumPlayers())
                gameThreadLock.wait();
        }
    }

    /**
     * Pauses the game thread until all players are ready to continue
     *
     * @author Alessandro
     */
    public void waitPlayersReady() throws InterruptedException {
        Game game = gameManager.getGame();

        synchronized (gameThreadLock) {
            while (game.getNumReadyPlayers() != game.getMaxNumPlayers())
                gameThreadLock.wait();
        }
    }

    /**
     * Pauses the game thread until all travelers are ready to continue
     *
     * @author Gabriele
     */
    public void waitTravelersReady() throws InterruptedException {
        Game game = gameManager.getGame();
        Board board = game.getBoard();

        for (Player player : board.getCopyTravelers()) {
            player.setIsReady(false, game);
        }

        synchronized (gameThreadLock) {
            while (!board.allTravelersReady()) {
                gameThreadLock.wait();
            }
        }
    }

    /**
     * Notifies the game thread
     */
    public void notifyThread(){
        synchronized (gameThreadLock){
            gameThreadLock.notify();
        }
    }

    /**
     * Stops the game thread
     */
    public void stopThread() {
        isRunning = false;
    }
}