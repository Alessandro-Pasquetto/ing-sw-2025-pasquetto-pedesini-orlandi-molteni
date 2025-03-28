package org.progetto.messages.toServer;

import java.io.Serializable;

public class PickVisibleComponent implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    int componentIdx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PickVisibleComponent(int componentIdx) {
        this.componentIdx = componentIdx;
    }

    // =======================
    // GETTERS
    // =======================

    public int getComponentIdx() {
        return componentIdx;
    }
}