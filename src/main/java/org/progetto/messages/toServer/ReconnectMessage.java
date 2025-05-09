package org.progetto.messages.toServer;

import java.io.Serializable;

public class ReconnectMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int gameId;
    private final String namePlayer;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ReconnectMessage(int gameId, String namePlayer) {
        this.gameId = gameId;
        this.namePlayer = namePlayer;
    }

    // =======================
    // GETTERS
    // =======================

    public int getGameId() {
        return gameId;
    }

    public String getNamePlayer() {
        return namePlayer;
    }
}