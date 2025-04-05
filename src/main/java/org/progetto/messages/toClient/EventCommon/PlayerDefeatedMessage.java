package org.progetto.messages.toClient.EventCommon;

import java.io.Serializable;

public class PlayerDefeatedMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String playerName;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlayerDefeatedMessage(String playerName) {
        this.playerName = playerName;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }
}
