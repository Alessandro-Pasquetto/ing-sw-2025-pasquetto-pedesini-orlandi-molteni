package org.progetto.server.controller;

import java.util.function.Consumer;

public class TimerController {
    // =======================
    // ATTRIBUTES
    // =======================
    Consumer<String> broadcastMessageFunction;
    private final int defaultTimer;
    private int timer;
    private int timerFlipsAllowed;

    // =======================
    // CONSTRUCTORS
    // =======================

    public TimerController(Consumer<String> broadcastMessageFunction, int defaultTimer, int timerFlipsAllowed) {
        this.broadcastMessageFunction = broadcastMessageFunction;
        this.defaultTimer = defaultTimer;
        this.timer = defaultTimer;
        this.timerFlipsAllowed = timerFlipsAllowed;
    }

    // =======================
    // GETTERS
    // =======================

    public synchronized int getTimer(){
        return timer;
    }

    // =======================
    // SETTERS
    // =======================

    public synchronized boolean resetTimer() {
        if (timer != 0 || timerFlipsAllowed == 0) {
            return false;
        }

        timer = defaultTimer;
        timerFlipsAllowed--;
        startTimer();
        return true;
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
                    broadcastMessageFunction.accept("Timer: " + timer);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (timer == 0) {
                if(timerFlipsAllowed == 0) {
                    System.out.println("Timer scaduto!");
                }
            }
        }).start();
    }
}
