package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponsePlanetLandRequestMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int idx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponsePlanetLandRequestMessage(int idx) {
        this.idx = idx;
    }

    // =======================
    // GETTERS
    // =======================

    public int getIdx() { return idx; }
}