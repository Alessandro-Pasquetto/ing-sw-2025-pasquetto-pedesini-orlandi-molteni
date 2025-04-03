package org.progetto.messages.toClient;

import java.io.Serializable;

public class AcceptRewardCreditsAndPenaltyDays implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    int rewardCredits;
    int penaltyDays;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AcceptRewardCreditsAndPenaltyDays(int rewardCredits, int penaltyDays) {
        this.rewardCredits = rewardCredits;
        this.penaltyDays = penaltyDays;
    }

    // =======================
    // GETTERS
    // =======================

    public int getRewardCredits() {
        return rewardCredits;
    }
    public int getPenaltyDays() {
        return penaltyDays;
    }
}