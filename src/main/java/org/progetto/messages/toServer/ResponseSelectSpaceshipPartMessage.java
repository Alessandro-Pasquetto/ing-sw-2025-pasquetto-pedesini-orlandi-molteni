package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponseSelectSpaceshipPartMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int x;
    private final int y;

    public ResponseSelectSpaceshipPartMessage(int x, int y) {
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