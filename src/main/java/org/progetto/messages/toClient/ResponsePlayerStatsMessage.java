package org.progetto.messages.toClient;

import org.progetto.server.model.Player;

import java.io.Serializable;

public class ResponsePlayerStatsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String playerName;
    private int credits;
    private int position;
    private boolean hasLeft;

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
