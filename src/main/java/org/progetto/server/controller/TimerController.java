package org.progetto.server.controller;

import org.progetto.messages.toClient.TimerMessage;

import java.util.function.Consumer;

/**
 * Timer controller class
 */
public class TimerController {

    // =======================
    // ATTRIBUTES
    // =======================

    Consumer<Object> broadcastMessageFunction;
    private final int defaultTimer;
    private int timer;
    private int timerFlipsAllowed;

    // =======================
    // CONSTRUCTORS
    // =======================

    public TimerController(Consumer<Object> broadcastMessageFunction, int defaultTimer, int timerFlipsAllowed) {
        this.broadcastMessageFunction = broadcastMessageFunction;
        this.defaultTimer = defaultTimer;
        this.timer = defaultTimer;
        this.timerFlipsAllowed = timerFlipsAllowed;
    }

    // =======================
    // GETTERS
    // =======================

    public synchronized int getTimerInt(){
        return timer;
    }

    // =======================
    // SETTERS
    // =======================

    public synchronized void resetTimer() throws IllegalStateException {
        if (timer != 0 || timerFlipsAllowed == 0) {
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
            while (timer > 0) {
                try {
                    Thread.sleep(1000);
                    timer--;
                    System.out.println("Timer: " + timer);
                    broadcastMessageFunction.accept(new TimerMessage(timer));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (timer == 0 && timerFlipsAllowed == 0) {
                broadcastMessageFunction.accept("Timer expired!");
                System.out.println("Timer expired");
            }
        }).start();
    }
}
