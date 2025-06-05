package org.progetto.messages.toClient.Battlezone;

import java.io.Serializable;

public class AnotherPlayerGotPenalizedMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String playerName;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerGotPenalizedMessage(String playerName) {
        this.playerName = playerName;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }
}
