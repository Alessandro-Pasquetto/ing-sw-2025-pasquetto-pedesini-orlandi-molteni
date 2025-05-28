package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class AnotherPlayerWonBattleMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String playerName;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerWonBattleMessage(String playerName) {
        this.playerName = playerName;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }
}
