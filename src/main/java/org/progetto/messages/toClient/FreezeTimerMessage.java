package org.progetto.messages.toClient;

import java.io.Serializable;

public class FreezeTimerMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int timer;

    // =======================
    // CONSTRUCTORS
    // =======================

    public FreezeTimerMessage(int timer) {
        this.timer = timer;
    }

    // =======================
    // GETTERS
    // =======================

    public int getTimer() {
        return timer;
    }
}