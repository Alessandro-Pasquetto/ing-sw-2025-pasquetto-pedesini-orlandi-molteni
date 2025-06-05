package org.progetto.messages.toClient.Populating;

import java.io.Serializable;

public class AlienPlacedMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int x;
    private final int y;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AlienPlacedMessage(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // =======================
    // GETTERS
    // =======================

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
