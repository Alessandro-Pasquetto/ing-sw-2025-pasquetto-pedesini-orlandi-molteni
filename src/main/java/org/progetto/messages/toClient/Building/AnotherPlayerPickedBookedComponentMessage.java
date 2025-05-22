package org.progetto.messages.toClient.Building;

import java.io.Serializable;

public class AnotherPlayerPickedBookedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================
    private final String playerName;
    private final int idx;

    // =======================
    // CONSTRUCTORS
    // =======================
    public AnotherPlayerPickedBookedComponentMessage(String playerName, int idx) {
        this.playerName = playerName;
        this.idx = idx;
    }

    // =======================
    // GETTERS
    // =======================


    public String getPlayerName() {
        return playerName;
    }

    public int getIdx() {
        return idx;
    }
}