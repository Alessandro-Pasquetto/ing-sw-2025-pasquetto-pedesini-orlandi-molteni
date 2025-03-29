package org.progetto.messages.toServer;

import java.io.Serializable;

public class PickUpEventCardDeck implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int deckIdx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PickUpEventCardDeck(int deckIdx) {
        this.deckIdx = deckIdx;
    }

    // =======================
    // GETTERS
    // =======================

    public int getDeckIdx() {
        return deckIdx;
    }
}
