package org.progetto.messages.toClient.Building;

import java.io.Serializable;

public class AnotherPlayerPutDownEventCardDeckMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String namePlayer;
    private int deckIdx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerPutDownEventCardDeckMessage(String namePlayer, int deckIdx) {
        this.namePlayer = namePlayer;
        this.deckIdx = deckIdx;
    }

    // =======================
    // GETTERS
    // =======================

    public String getNamePlayer() {
        return namePlayer;
    }

    public int getDeckIdx() {
        return deckIdx;
    }
}
