package org.progetto.messages.toServer;

import java.io.Serializable;

public class DestroyComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int x;
    private final int y;

    // =======================
    // CONSTRUCTORS
    // =======================

    public DestroyComponentMessage(int x, int y) {
        this.x = x;
        this.y = y;
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

}