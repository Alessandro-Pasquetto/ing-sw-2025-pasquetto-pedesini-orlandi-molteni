package org.progetto.messages.toClient;

import java.io.Serializable;

public class ResponsePlayerStatsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String playerName;
    private final int credits;
    private final int position;
    private final boolean hasLeft;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponsePlayerStatsMessage(String playerName, int credits, int position, boolean hasLeft) {
        this.playerName = playerName;
        this.credits = credits;
        this.position = position;
        this.hasLeft = hasLeft;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }

    public int getCredits() {
        return credits;
    }

    public int getPosition() {
        return position;
    }

    public boolean getHasLeft() {
        return hasLeft;
    }
}
