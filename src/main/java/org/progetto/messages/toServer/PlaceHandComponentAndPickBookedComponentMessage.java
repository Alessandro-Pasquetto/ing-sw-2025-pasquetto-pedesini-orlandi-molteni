package org.progetto.messages.toServer;

import java.io.Serializable;

public class PlaceHandComponentAndPickBookedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int x;
    private int y;
    private int rotation;
    private int idx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlaceHandComponentAndPickBookedComponentMessage(int x, int y, int rotation, int idx) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.idx = idx;
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

    public int getIdx() {
        return idx;
    }
}
