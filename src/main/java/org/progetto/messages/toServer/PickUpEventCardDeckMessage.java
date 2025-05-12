package org.progetto.messages.toServer;

import java.io.Serializable;

public class PickUpEventCardDeckMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int deckIdx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PickUpEventCardDeckMessage(int deckIdx) {
        this.deckIdx = deckIdx;
    }

    // =======================
    // GETTERS
    // =======================

    public int getDeckIdx() {
        return deckIdx;
    }
}
