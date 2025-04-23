package org.progetto.messages.toClient.EventCommon;

import java.io.Serializable;

public class PlayerGetsCreditsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int credits;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlayerGetsCreditsMessage(int credits) {
        this.credits = credits;
    }

    // =======================
    // GETTERS
    // =======================

    public int getCredits() {
        return credits;
    }
}