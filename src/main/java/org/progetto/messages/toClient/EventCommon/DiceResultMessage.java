package org.progetto.messages.toClient.EventCommon;

import java.io.Serializable;

public class DiceResultMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int diceResult;

    // =======================
    // CONSTRUCTORS
    // =======================

    public DiceResultMessage(int diceResult) {
        this.diceResult = diceResult;
    }

    // =======================
    // GETTERS
    // =======================

    public int getDiceResult() {
        return diceResult;
    }
}
