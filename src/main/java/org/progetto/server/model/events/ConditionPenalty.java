package org.progetto.server.model.events;

public class ConditionPenalty {

    // =======================
    // ATTRIBUTES
    // =======================

    private ConditionType condition;
    private Penalty penalty;

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