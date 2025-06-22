package org.progetto.server.controller;

import org.progetto.messages.toClient.Building.TimerMessage;
import org.progetto.messages.toClient.FreezeTimerMessage;
import org.progetto.server.connection.games.GameManager;

public class FreezeTimerThread {

    // =======================
    // ATTRIBUTES
    // =======================

    private final GameManager gameManager;
    private final int defaultTimer;
    private int timer;
    private boolean isTimerRunning;

    // =======================
    // CONSTRUCTORS
    // =======================

    public FreezeTimerThread(GameManager gameManager, int defaultTimer) {
        this.gameManager = gameManager;
        this.defaultTimer = defaultTimer;
        this.isTimerRunning = false;
    }

    /**
     * Starts freeze timer
     *
     * @author Alessandro
     */
    public synchronized void startTimer() {
        timer = defaultTimer;
        isTimerRunning = true;

        new Thread(() -> {

            int currentTimer = timer;
            while (isTimerRunning) {

                gameManager.broadcastGameMessage(new FreezeTimerMessage(currentTimer));

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

            isTimerRunning = false;

            if(timer == 0)
                gameManager.broadcastGameMessage("WonByForfeit");
        }).start();
    }

    /**
     * Stops the freeze timer
     *
     * @author Alessandro
     */
    public void stopTimer() {
        isTimerRunning = false;
    }
}