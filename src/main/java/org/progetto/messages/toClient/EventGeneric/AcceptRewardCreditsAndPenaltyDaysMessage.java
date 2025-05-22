package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class AcceptRewardCreditsAndPenaltyDaysMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int rewardCredits;
    private final int penaltyDays;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AcceptRewardCreditsAndPenaltyDaysMessage(int rewardCredits, int penaltyDays) {
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