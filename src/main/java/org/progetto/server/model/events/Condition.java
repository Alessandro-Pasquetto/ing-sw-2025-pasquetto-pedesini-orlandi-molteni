package org.progetto.server.model.events;

public class Condition {

    // =======================
    // ATTRIBUTES
    // =======================

    private ConditionType Condition;
    private boolean greater;
    private boolean fewer;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Condition(ConditionType condition, boolean greater, boolean fewer) {
        Condition = condition;
        this.greater = greater;
        this.fewer = fewer;
    }

    // =======================
    // GETTERS
    // =======================

    public boolean isFewer() {
        return fewer;
    }

    public boolean isGreater() {
        return greater;
    }

    public ConditionType getCondition() {
        return Condition;
    }

}