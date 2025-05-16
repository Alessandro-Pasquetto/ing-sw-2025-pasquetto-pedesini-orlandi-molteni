package org.progetto.messages.toClient.Building;

import org.progetto.server.model.Player;

import java.io.Serializable;

public class AnotherPlayerSetStartingPositionMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String playerName;
    private int startingPosition;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerSetStartingPositionMessage(String playerName, int startingPosition) {
        this.playerName = playerName;
        this.startingPosition = startingPosition;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }

    public int getStartingPositions() {
        return startingPosition;
    }
}
