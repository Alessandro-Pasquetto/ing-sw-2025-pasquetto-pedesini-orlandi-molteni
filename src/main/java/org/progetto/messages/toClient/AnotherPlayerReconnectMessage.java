package org.progetto.messages.toClient;

import java.io.Serializable;

public class AnotherPlayerReconnectMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String playerName;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerReconnectMessage(String playerName) {
        this.playerName = playerName;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }
}