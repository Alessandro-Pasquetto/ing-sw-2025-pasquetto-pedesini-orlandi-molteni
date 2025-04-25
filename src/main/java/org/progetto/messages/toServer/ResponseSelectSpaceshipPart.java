package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponseSelectSpaceshipPart implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int x;
    private final int y;

    public ResponseSelectSpaceshipPart(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}