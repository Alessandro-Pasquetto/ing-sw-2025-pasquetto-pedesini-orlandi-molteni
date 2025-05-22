package org.progetto.messages.toClient.Travel;

import java.io.Serializable;

public class PlayerIsContinuingMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String playerName;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlayerIsContinuingMessage(String playerName) {
        this.playerName = playerName;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }
}
