package org.progetto.messages.toClient;

import java.io.Serializable;

public class PlayerMovedBackwardsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int stepsCount;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlayerMovedBackwardsMessage(int stepsCount) {
        this.stepsCount = stepsCount;
    }

    // =======================
    // GETTERS
    // =======================

    public int getStepsCount() {
        return stepsCount;
    }
}