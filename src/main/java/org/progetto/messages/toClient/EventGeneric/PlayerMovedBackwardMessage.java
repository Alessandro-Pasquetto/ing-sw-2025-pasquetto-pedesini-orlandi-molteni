package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class PlayerMovedBackwardMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int stepsCount;

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
