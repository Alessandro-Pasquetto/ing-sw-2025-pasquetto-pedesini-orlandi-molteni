package org.progetto.messages.toServer;

import java.io.Serializable;

public class DestroyComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int y;
    private int x;

    // =======================
    // CONSTRUCTORS
    // =======================

    public DestroyComponentMessage(int y, int x) {
        this.y = y;
        this.x = x;
    }

    // =======================
    // GETTERS
    // =======================

    public int getY() {
        return y;
    }
    public int getX() {
        return x;
    }

}