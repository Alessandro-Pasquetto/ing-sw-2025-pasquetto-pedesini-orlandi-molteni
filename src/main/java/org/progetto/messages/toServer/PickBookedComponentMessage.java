package org.progetto.messages.toServer;

import java.io.Serializable;

public class PickBookedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================
    private final int idx;

    // =======================
    // CONSTRUCTORS
    // =======================
    public PickBookedComponentMessage(int idx) {
        this.idx = idx;
    }

    // =======================
    // GETTERS
    // =======================
    public int getIdx() {
        return idx;
    }
}