package org.progetto.messages.toClient.OpenSpace;

import java.io.Serializable;

public class PlayerMovedAheadMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int stepsCount;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlayerMovedAheadMessage(int stepsCount) {
        this.stepsCount = stepsCount;
    }

    // =======================
    // GETTERS
    // =======================

    public int getStepsCount() {
        return stepsCount;
    }
}
