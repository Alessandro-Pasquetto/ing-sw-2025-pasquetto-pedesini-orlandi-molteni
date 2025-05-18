package org.progetto.messages.toClient.Positioning;

import org.progetto.server.model.Player;

import java.io.Serializable;

public class StartingPositionsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Player[] startingPositions;

    // =======================
    // CONSTRUCTORS
    // =======================

    public StartingPositionsMessage(Player[] startingPositions) {
        this.startingPositions = startingPositions;
    }

    // =======================
    // GETTERS
    // =======================

    public Player[] getStartingPositions() {
        return startingPositions;
    }
}
