package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class AnotherPlayerDrewBattleMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String playerName;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerDrewBattleMessage(String playerName) {
        this.playerName = playerName;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }
}
