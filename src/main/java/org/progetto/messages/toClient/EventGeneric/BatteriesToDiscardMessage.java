package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class BatteriesToDiscardMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int batteriesToDiscard;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BatteriesToDiscardMessage(int batteriesToDiscard) {
        this.batteriesToDiscard = batteriesToDiscard;
    }

    // =======================
    // GETTERS
    // =======================

    public int getBatteriesToDiscard() {
        return batteriesToDiscard;
    }
}
