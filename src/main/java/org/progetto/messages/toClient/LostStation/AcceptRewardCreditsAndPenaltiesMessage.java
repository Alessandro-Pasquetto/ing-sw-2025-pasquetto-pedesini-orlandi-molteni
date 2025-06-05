package org.progetto.messages.toClient.LostStation;

import java.io.Serializable;

public class AcceptRewardCreditsAndPenaltiesMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int rewardCredits;
    private final int penaltyCrew;
    private final int penaltyDays;

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