package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponseAcceptRewardBoxesAndPenaltyDaysMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String response;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseAcceptRewardBoxesAndPenaltyDaysMessage(String response) { this.response = response; }

    // =======================
    // GETTERS
    // =======================

    public String getResponse() { return response; }
}
