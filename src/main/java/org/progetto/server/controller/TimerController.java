package org.progetto.server.controller;

import org.progetto.messages.toClient.TimerMessage;
import org.progetto.server.connection.games.GameCommunicationHandler;

import java.rmi.RemoteException;

/**
 * Timer controller class
 */
public class TimerController {

    // =======================
    // ATTRIBUTES
    // =======================

    GameCommunicationHandler gameCommunicationHandler;
    private final int defaultTimer;
    private int timer;
    private int timerFlipsAllowed;

    // =======================
    // CONSTRUCTORS
    // =======================

    public TimerController(GameCommunicationHandler gameCommunicationHandler, int defaultTimer, int timerFlipsAllowed) {
        this.gameCommunicationHandler = gameCommunicationHandler;
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
            while (true) {
                int currentTimer;

                synchronized (this) {
                    if (timer <= 0)
                        break;

                    timer--;
                    currentTimer = timer;
                }

                System.out.println("Timer: " + currentTimer);
                gameCommunicationHandler.broadcastGameMessage(new TimerMessage(currentTimer));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (timerFlipsAllowed == 0) {
                gameCommunicationHandler.broadcastGameMessage("Timer expired!");
                System.out.println("Timer expired");
                BuildingController.checkShipValidity(gameCommunicationHandler);
            }
        }).start();
    }
}
