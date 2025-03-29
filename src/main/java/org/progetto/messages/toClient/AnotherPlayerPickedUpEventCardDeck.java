package org.progetto.messages.toClient;

import java.io.Serializable;

public class AnotherPlayerPickedUpEventCardDeck implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String namePlayer;
    private int deckIdx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerPickedUpEventCardDeck(String namePlayer, int deckIdx) {
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