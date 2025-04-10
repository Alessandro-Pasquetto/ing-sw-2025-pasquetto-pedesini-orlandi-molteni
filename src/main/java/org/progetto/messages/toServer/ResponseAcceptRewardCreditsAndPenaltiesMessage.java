package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponseAcceptRewardCreditsAndPenaltiesMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String response;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseAcceptRewardCreditsAndPenaltiesMessage(String response) { this.response = response; }

    // =======================
    // GETTERS
    // =======================

    public String getResponse() { return response; }
}