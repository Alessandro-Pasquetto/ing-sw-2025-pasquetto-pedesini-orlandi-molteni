package org.progetto.server.model.events;

public class ConditionPenalty {

    // =======================
    // ATTRIBUTES
    // =======================

    private ConditionType type;
    private Penalty penalty;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ConditionPenalty(ConditionType condition, Penalty penalty) {
        this.type = condition;
        this.penalty = penalty;
    }

    // =======================
    // GETTERS
    // =======================

    public ConditionType getType() {
        return type;
    }

    public Penalty getPenalty() {
        return penalty;
    }

}