package org.progetto.messages.toClient;

import java.io.Serializable;

public class DiceResultMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    int diceResult;

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
