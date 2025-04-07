package org.progetto.messages.toServer;

import java.io.Serializable;

public class PickVisibleComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int componentIdx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PickVisibleComponentMessage(int componentIdx) {
        this.componentIdx = componentIdx;
    }

    // =======================
    // GETTERS
    // =======================

    public int getComponentIdx() {
        return componentIdx;
    }
}