package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponseChooseToUseShieldMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String response;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseChooseToUseShieldMessage(String response) { this.response = response; }

    // =======================
    // GETTERS
    // =======================

    public String getResponse() { return response; }
}