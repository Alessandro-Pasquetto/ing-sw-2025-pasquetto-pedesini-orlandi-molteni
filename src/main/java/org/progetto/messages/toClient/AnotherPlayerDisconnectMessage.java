package org.progetto.messages.toClient;

import java.io.Serializable;

public class AnotherPlayerDisconnectMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String playerName;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerDisconnectMessage(String playerName) {
        this.playerName = playerName;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }
}