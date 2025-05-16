package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponseStartingPositionMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int startingPosition;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseStartingPositionMessage(int startingPosition) {
        this.startingPosition = startingPosition;
    }

    // =======================
    // GETTERS
    // =======================

    public int getStartingPosition() {
        return startingPosition;
    }
}
