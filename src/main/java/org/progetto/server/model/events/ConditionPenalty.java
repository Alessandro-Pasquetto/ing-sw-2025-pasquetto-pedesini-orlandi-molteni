package org.progetto.server.model.events;

public class ConditionPenalty {

    // =======================
    // ATTRIBUTES
    // =======================

    private Condition condition;
    private Penalty penalty;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ConditionPenalty(Condition condition, Penalty penalty) {
        this.condition = condition;
        this.penalty = penalty;
    }

    // =======================
    // GETTERS
    // =======================

    public Condition getCondition() {
        return condition;
    }

    public Penalty getPenalty() {
        return penalty;
    }

}