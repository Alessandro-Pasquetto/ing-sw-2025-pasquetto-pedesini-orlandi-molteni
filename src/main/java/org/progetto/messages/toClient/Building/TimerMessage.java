package org.progetto.messages.toClient.Building;

import java.io.Serializable;

public class TimerMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int time;

    // =======================
    // CONSTRUCTORS
    // =======================

    public TimerMessage(int time) {
        this.time = time;
    }

    // =======================
    // GETTERS
    // =======================

    public int getTime() {
        return time;
    }
}
