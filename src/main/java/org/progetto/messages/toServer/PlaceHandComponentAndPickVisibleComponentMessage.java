package org.progetto.messages.toServer;

public class PlaceHandComponentAndPickVisibleComponentMessage {

    // =======================
    // ATTRIBUTES
    // =======================

    int x;
    int y;
    int rotation;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlaceHandComponentAndPickVisibleComponentMessage(int x, int y, int rotation) {
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
