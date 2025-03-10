package org.progetto.server.model;

public class Timer {
    // =======================
    // ATTRIBUTES
    // =======================

    private static int defaultTimer;
    private static int timer;
    private static int timerFlipsAllowed;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Timer(int defaultTimer, int timerFlipsAllowed) {
        Timer.defaultTimer = defaultTimer;
        Timer.timer = defaultTimer;
        Timer.timerFlipsAllowed = timerFlipsAllowed;
    }

    // =======================
    // GETTERS
    // =======================

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
