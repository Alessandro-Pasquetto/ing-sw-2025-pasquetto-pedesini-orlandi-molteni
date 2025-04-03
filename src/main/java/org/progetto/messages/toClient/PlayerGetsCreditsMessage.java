package org.progetto.messages.toClient;

import java.io.Serializable;

public class PlayerGetsCreditsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int credits;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlayerGetsCreditsMessage(int stepsCount) {
        this.credits = credits;
    }

    // =======================
    // GETTERS
    // =======================

    public int getCredits() {
        return credits;
    }
}