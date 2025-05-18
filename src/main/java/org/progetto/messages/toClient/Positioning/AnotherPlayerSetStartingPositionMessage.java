package org.progetto.messages.toClient.Positioning;

import org.progetto.server.model.Player;

import java.io.Serializable;

public class AnotherPlayerSetStartingPositionMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String playerName;
    private Player[] startingPositions;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerSetStartingPositionMessage(String playerName, Player[] startingPositions) {
        this.playerName = playerName;
        this.startingPositions = startingPositions;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }

    public Player[] getStartingPositions() {
        return startingPositions;
    }
}
