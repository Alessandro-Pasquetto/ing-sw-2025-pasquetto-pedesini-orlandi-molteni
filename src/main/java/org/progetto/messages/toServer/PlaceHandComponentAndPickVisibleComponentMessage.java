package org.progetto.messages.toServer;

import java.io.Serializable;

public class PlaceHandComponentAndPickVisibleComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int x;
    private int y;
    private int rotation;
    private int componentIdx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlaceHandComponentAndPickVisibleComponentMessage(int x, int y, int rotation, int componentIdx) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.componentIdx = componentIdx;
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

    public int getComponentIdx() {
        return componentIdx;
    }
}
