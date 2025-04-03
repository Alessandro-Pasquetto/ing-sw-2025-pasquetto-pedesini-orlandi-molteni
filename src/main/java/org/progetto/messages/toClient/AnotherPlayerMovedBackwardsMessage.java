package org.progetto.messages.toClient;

import java.io.Serializable;

public class AnotherPlayerMovedBackwardsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String namePlayer;
    private int stepsCount;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerMovedBackwardsMessage(String namePlayer, int stepsCount) {
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