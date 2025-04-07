package org.progetto.server.controller;

import org.progetto.messages.toClient.Building.TimerMessage;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.GamePhase;

/**
 * Timer controller class
 */
public class TimerController {

    // =======================
    // ATTRIBUTES
    // =======================

    private final GameManager gameManager;
    private final int defaultTimer;
    private int timer;
    private int timerFlipsAllowed;
    private boolean isTimerRunning = false;

    // =======================
    // CONSTRUCTORS
    // =======================

    public TimerController(GameManager gameManager, int defaultTimer, int timerFlipsAllowed) {
        this.gameManager = gameManager;
        this.defaultTimer = defaultTimer;
        this.timer = defaultTimer;
        this.timerFlipsAllowed = timerFlipsAllowed;
    }

    // =======================
    // GETTERS
    // =======================

    public synchronized boolean isTimerExpired() {
        return !(isTimerRunning || timerFlipsAllowed > 0);
    }

    // =======================
    // SETTERS
    // =======================

    public synchronized void resetTimer() throws IllegalStateException {
        if (gameManager.getGame().getPhase() != GamePhase.BUILDING || isTimerRunning || timerFlipsAllowed == 0) {
            throw new IllegalStateException("ImpossibleToResetTimer");
        }

        timer = defaultTimer;
        timerFlipsAllowed--;
        startTimer();
    }

    // =======================
    // OTHER METHODS
    // =======================

    public synchronized void startTimer() {

        new Thread(() -> {
            isTimerRunning = true;

            int currentTimer = timer;
            while (isTimerRunning) {

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
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (timerFlipsAllowed == 0) {
                gameManager.getGameThread().notifyThread();
            }

            isTimerRunning = false;
        }).start();
    }

    public synchronized void stopTimer() {
        isTimerRunning = false;
    }
}
