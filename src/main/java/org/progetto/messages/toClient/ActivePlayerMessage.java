package org.progetto.messages.toClient;

import java.io.Serializable;

public class ActivePlayerMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String playerName;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ActivePlayerMessage(String playerName) {
        this.playerName = playerName;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }
}
