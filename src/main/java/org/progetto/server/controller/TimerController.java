package org.progetto.server.controller;

import org.progetto.messages.toClient.TimerMessage;
import org.progetto.server.connection.games.GameManager;

/**
 * Timer controller class
 */
public class TimerController {

    // =======================
    // ATTRIBUTES
    // =======================

    GameManager gameManager;
    private final int defaultTimer;
    private int timer;
    private int timerFlipsAllowed;

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
        return !(timer > 0 || timerFlipsAllowed > 0);
    }

    // =======================
    // SETTERS
    // =======================

    public synchronized void resetTimer() throws IllegalStateException {
        if (timer > 0 || timerFlipsAllowed == 0) {
            throw new IllegalStateException("ImpossibleToResetTimer");
        }

        timer = defaultTimer;
        timerFlipsAllowed--;
        startTimer();
    }

    // =======================
    // OTHER METHODS
    // =======================

    public void startTimer() {

        new Thread(() -> {
            int currentTimer = timer;
            while (true) {

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
                gameManager.broadcastGameMessage("Timer expired!");
                System.out.println("Timer expired");
                BuildingController.checkShipValidity(gameManager);
            }
        }).start();
    }
}
