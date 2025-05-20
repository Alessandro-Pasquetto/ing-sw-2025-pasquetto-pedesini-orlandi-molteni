package org.progetto.messages.toClient;

import java.io.Serializable;

public class PlayerColorMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int color;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlayerColorMessage(int color) {
        this.color = color;
    }

    // =======================
    // GETTERS
    // =======================

    public int getColor() {
        return color;
    }
}