package org.progetto.server.controller;

import org.progetto.messages.toClient.Building.TimerMessage;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.GamePhase;


public class TimerThreadController {

    // =======================
    // ATTRIBUTES
    // =======================

    private final GameManager gameManager;
    private final int defaultTimer;
    private int timer;
    private int timerFlipsAllowed;
    private boolean isTimerRunning = false;
    private boolean isTimerExpired = false;

    private final Object timerThreadLock = new Object();

    // =======================
    // CONSTRUCTORS
    // =======================

    public TimerThreadController(GameManager gameManager, int defaultTimer, int timerFlipsAllowed) {
        this.gameManager = gameManager;
        this.defaultTimer = defaultTimer;
        this.timer = defaultTimer;
        this.timerFlipsAllowed = timerFlipsAllowed;
    }

    // =======================
    // GETTERS
    // =======================

    public synchronized boolean getIsTimerExpired() {
        return isTimerExpired;
    }

    public synchronized int getTimerFlipsAllowed() {
        return timerFlipsAllowed;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Starts the timer
     *
     * @author Alessandro
     */
    public synchronized void startTimer() {
        isTimerRunning = true;

        new Thread(() -> {

            int currentTimer = timer;
            while (isTimerRunning) {

                synchronized (timerThreadLock) {
                    while(gameManager.getGame().getPlayersSize() == 1) {
                        try {
                            timerThreadLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                System.out.println("Timer: " + currentTimer);
                gameManager.broadcastGameMessage(new TimerMessage(currentTimer));

                synchronized (this) {
                    if (timer <= 0)
                        break;

                    timer--;
                    currentTimer = timer;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (timerFlipsAllowed == 0) {
                isTimerExpired = true;
                gameManager.getGameThread().notifyThread();
            }

            isTimerRunning = false;
        }).start();
    }

    /**
     * Resets the timer
     *
     * @author Alessandro
     * @throws IllegalStateException if the timer cannot be reset
     */
    public synchronized void resetTimer() throws IllegalStateException {
        if (gameManager.getGame().getPhase() != GamePhase.BUILDING || gameManager.getGame().getLevel() == 1 || isTimerRunning || timerFlipsAllowed == 0) {
            throw new IllegalStateException("ImpossibleToResetTimer");
        }

        timer = defaultTimer;
        timerFlipsAllowed--;
        startTimer();

        gameManager.broadcastGameMessage("TimerFlipped");
    }

    /**
     * Stops the timer
     *
     * @author Alessandro
     */
    public synchronized void stopTimer() {
        isTimerRunning = false;
    }

    public void notifyThread(){
        synchronized (timerThreadLock) {
            timerThreadLock.notify();
        }
    }
}
