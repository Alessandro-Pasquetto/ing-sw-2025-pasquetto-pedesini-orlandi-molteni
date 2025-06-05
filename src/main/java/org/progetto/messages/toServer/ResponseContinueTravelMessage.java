package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponseContinueTravelMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String response;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseContinueTravelMessage(String response) {
        this.response = response;
    }

    // =======================
    // GETTERS
    // =======================

    public String getResponse() {
        return response;
    }
}
