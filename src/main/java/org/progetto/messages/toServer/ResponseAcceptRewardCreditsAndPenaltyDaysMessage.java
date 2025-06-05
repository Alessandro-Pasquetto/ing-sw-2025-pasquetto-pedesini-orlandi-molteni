package org.progetto.messages.toServer;

import java.io.Serializable;

    public class ResponseAcceptRewardCreditsAndPenaltyDaysMessage implements Serializable {

        // =======================
        // ATTRIBUTES
        // =======================

        private final String response;

        // =======================
        // CONSTRUCTORS
        // =======================

        public ResponseAcceptRewardCreditsAndPenaltyDaysMessage(String response) { this.response = response; }

        // =======================
        // GETTERS
        // =======================

        public String getResponse() { return response; }
    }