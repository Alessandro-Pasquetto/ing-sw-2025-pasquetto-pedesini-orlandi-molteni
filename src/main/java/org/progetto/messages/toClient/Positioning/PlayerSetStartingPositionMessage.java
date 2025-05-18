package org.progetto.messages.toClient.Positioning;

import org.progetto.server.model.Player;

import java.io.Serializable;

public class PlayerSetStartingPositionMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private Player[] startingPositions;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlayerSetStartingPositionMessage(Player[] startingPositions) {
        this.startingPositions = startingPositions;
    }

    // =======================
    // GETTERS
    // =======================

    public Player[] getStartingPositions() {
        return startingPositions;
    }
}
