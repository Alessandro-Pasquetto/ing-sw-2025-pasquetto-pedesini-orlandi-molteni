package org.progetto.messages.toClient;

import java.io.Serializable;

public class AcceptRewardCreditsAndPenaltiesMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    int rewardCredits;
    int penaltyCrew;
    int penaltyDays;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AcceptRewardCreditsAndPenaltiesMessage(int rewardCredits, int penaltyCrew, int penaltyDays) {
        this.rewardCredits = rewardCredits;
        this.penaltyCrew = penaltyCrew;
        this.penaltyDays = penaltyDays;
    }

    // =======================
    // GETTERS
    // =======================

    public int getRewardCredits() {
        return rewardCredits;
    }
    public int getPenaltyCrew() {return penaltyCrew; }
    public int getPenaltyDays() {
        return penaltyDays;
    }
}