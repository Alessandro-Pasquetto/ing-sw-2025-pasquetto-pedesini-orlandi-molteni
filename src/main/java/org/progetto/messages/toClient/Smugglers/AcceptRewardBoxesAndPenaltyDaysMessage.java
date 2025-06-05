package org.progetto.messages.toClient.Smugglers;


import org.progetto.server.model.components.Box;

import java.io.Serializable;
import java.util.ArrayList;

public class AcceptRewardBoxesAndPenaltyDaysMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<Box> rewardBoxes;
    private final int penaltyDays;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AcceptRewardBoxesAndPenaltyDaysMessage(ArrayList<Box> rewardBoxes, int penaltyDays) {
        this.rewardBoxes = rewardBoxes;
        this.penaltyDays = penaltyDays;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Box> getRewardBoxes() {
        return rewardBoxes;
    }
    public int getPenaltyDays() {
        return penaltyDays;
    }
}