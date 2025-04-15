package org.progetto.messages.toClient.Sabotage;

import java.io.Serializable;

public class LessPopulatedPlayerMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String playerName;

    // =======================
    // CONSTRUCTORS
    // =======================

    public LessPopulatedPlayerMessage(String playerName) {
        this.playerName = playerName;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }
}
