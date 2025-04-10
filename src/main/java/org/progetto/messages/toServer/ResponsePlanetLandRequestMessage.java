package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponsePlanetLandRequestMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String response;
    private int idx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponsePlanetLandRequestMessage(String response, int idx) {
        this.response = response;
        this.idx = idx;
    }

    // =======================
    // GETTERS
    // =======================

    public String getResponse() { return response; }

    public int getIdx() { return idx; }
}