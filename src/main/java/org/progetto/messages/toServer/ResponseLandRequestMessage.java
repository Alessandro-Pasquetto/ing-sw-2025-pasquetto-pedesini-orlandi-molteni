package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponseLandRequestMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String response;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseLandRequestMessage(String response) { this.response = response; }

    // =======================
    // GETTERS
    // =======================

    public String getResponse() { return response; }
}