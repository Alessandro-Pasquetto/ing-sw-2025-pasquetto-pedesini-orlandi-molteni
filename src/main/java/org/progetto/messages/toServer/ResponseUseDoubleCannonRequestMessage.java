package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponseUseDoubleCannonRequestMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String response;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseUseDoubleCannonRequestMessage(String response) { this.response = response; }

    // =======================
    // GETTERS
    // =======================

    public String getResponse() { return response; }
}