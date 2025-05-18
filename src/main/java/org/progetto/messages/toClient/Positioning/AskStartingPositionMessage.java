package org.progetto.messages.toClient.Positioning;

import org.progetto.server.model.Player;

import java.io.Serializable;

public class AskStartingPositionMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Player[] startingPositions;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AskStartingPositionMessage(Player[] startingPositions) {
        this.startingPositions = startingPositions;
    }

    // =======================
    // GETTERS
    // =======================

    public Player[] getStartingPositions() {
        return startingPositions;
    }
}
