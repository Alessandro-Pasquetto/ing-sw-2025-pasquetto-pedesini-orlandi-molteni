package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class AnotherPlayerGetsCreditsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String namePlayer;
    private final int credits;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerGetsCreditsMessage(String namePlayer, int credits) {
        this.namePlayer = namePlayer;
        this.credits = credits;
    }

    // =======================
    // GETTERS
    // =======================

    public String getNamePlayer() {
        return namePlayer;
    }

    public int getCredits() {
        return credits;
    }
}