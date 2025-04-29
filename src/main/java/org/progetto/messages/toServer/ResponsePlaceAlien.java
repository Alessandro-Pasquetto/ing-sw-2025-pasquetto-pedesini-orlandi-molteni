package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponsePlaceAlien implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int x;
    private final int y;
    private final String color;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponsePlaceAlien(int x, int y, String color) {
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