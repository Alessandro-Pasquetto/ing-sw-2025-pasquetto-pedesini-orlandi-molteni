package org.progetto.messages.toClient;

import java.io.Serializable;

public class PlayerMovedBackwardMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int stepsCount;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlayerMovedBackwardMessage(int stepsCount) {
        this.stepsCount = stepsCount;
    }

    // =======================
    // GETTERS
    // =======================

    public int getStepsCount() {
        return stepsCount;
    }
}
