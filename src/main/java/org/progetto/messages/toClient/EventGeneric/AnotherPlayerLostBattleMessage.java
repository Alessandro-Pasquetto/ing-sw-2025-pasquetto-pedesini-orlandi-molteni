package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class AnotherPlayerLostBattleMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String playerName;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerLostBattleMessage(String playerName) {
        this.playerName = playerName;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }
}
