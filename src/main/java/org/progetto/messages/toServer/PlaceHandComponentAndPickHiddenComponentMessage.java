package org.progetto.messages.toServer;

import java.io.Serializable;

public class PlaceHandComponentAndPickHiddenComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int x;
    private int y;
    private int rotation;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlaceHandComponentAndPickHiddenComponentMessage(int x, int y, int rotation) {
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