package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ConditionPenaltyTest {

    @Test
    void getType() {
        Penalty penalty = new Penalty(PenaltyType.PENALTYDAYS, -3, new ArrayList<>());
        ConditionPenalty conditionPenalty = new ConditionPenalty(ConditionType.CREWREQUIREMENT, penalty);

        assertEquals(ConditionType.CREWREQUIREMENT, conditionPenalty.getType());
    }

    @Test
    void getPenalty() {
        Penalty penalty = new Penalty(PenaltyType.PENALTYDAYS, -3, new ArrayList<>());
        ConditionPenalty conditionPenalty = new ConditionPenalty(ConditionType.CREWREQUIREMENT, penalty);

        assertEquals(penalty, conditionPenalty.getPenalty());
    }
}