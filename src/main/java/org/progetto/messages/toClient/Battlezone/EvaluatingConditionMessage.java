package org.progetto.messages.toClient.Battlezone;

import java.io.Serializable;

public class EvaluatingConditionMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String condition;

    // =======================
    // CONSTRUCTORS
    // =======================

    public EvaluatingConditionMessage(String condition) {
        this.condition = condition;
    }

    // =======================
    // GETTERS
    // =======================

    public String getCondition() {
        return condition;
    }
}
