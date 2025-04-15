package org.progetto.messages.toClient.OpenSpace;

import java.io.Serializable;

public class AnotherPlayerMovedAheadMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String namePlayer;
    private int stepsCount;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerMovedAheadMessage(String namePlayer, int stepsCount) {
        this.namePlayer = namePlayer;
        this.stepsCount = stepsCount;
    }

    // =======================
    // GETTERS
    // =======================

    public String getNamePlayer() {
        return namePlayer;
    }

    public int getStepsCount() {
        return stepsCount;
    }
}
