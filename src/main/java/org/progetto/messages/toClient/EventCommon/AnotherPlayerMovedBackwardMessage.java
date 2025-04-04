package org.progetto.messages.toClient.EventCommon;

import java.io.Serializable;

public class AnotherPlayerMovedBackwardMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String namePlayer;
    private int stepsCount;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerMovedBackwardMessage(String namePlayer, int stepsCount) {
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
