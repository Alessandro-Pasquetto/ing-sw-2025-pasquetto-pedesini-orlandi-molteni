package org.progetto.server.model.events;

import java.io.Serializable;

public class ConditionPenalty implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ConditionType condition;
    private final Penalty penalty;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ConditionPenalty(ConditionType condition, Penalty penalty) {
        this.condition = condition;
        this.penalty = penalty;
    }

    // =======================
    // GETTERS
    // =======================

    public ConditionType getCondition() {
        return condition;
    }

    public Penalty getPenalty() {
        return penalty;
    }

}