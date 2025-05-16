package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponsePlaceAlienMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int x;
    private final int y;
    private final String color;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponsePlaceAlienMessage(int x, int y, String color) {
        this.x = x;
        this.y = y;
        this.color = color;
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

    public String getColor() {
        return color;
    }
}