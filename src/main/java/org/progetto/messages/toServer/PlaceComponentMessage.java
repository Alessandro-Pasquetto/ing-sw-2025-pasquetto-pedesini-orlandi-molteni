package org.progetto.messages.toServer;

import java.io.Serializable;

public class PlaceComponentMessage implements Serializable {

    private final int x;
    private final int y;
    private final int rotation;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlaceComponentMessage(int x, int y, int rotation) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
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

    public int getRotation() {
        return rotation;
    }
}