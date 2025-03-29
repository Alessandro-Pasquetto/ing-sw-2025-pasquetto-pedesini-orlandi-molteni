package org.progetto.messages.toServer;

import java.io.Serializable;

public class PickVisibleComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    int componentIdx;

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