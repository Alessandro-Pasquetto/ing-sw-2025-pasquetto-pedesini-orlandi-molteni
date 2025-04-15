package org.progetto.messages.toClient.EventCommon;

import java.io.Serializable;

public class AnotherPlayerDiceResultMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String namePlayer;
    private int diceResult;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerDiceResultMessage(String namePlayer, int diceResult) {
        this.namePlayer = namePlayer;
        this.diceResult = diceResult;
    }

    // =======================
    // GETTERS
    // =======================

    public String getNamePlayer() {
        return namePlayer;
    }

    public int getDiceResult() {
        return diceResult;
    }
}
