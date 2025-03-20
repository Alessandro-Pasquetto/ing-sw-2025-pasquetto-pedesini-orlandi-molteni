package org.progetto.messages;

import java.io.Serializable;

public class PlaceHandComponentAndPickComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================
    int x;
    int y;
    int rotation;

    // =======================
    // CONSTRUCTORS
    // =======================
    public PlaceHandComponentAndPickComponentMessage(int x, int y, int rotation) {
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