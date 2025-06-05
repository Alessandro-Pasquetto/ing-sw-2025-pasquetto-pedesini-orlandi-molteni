package org.progetto.messages.toClient.Building;

import java.io.Serializable;

public class AnotherPlayerPickedUpEventCardDeck implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String namePlayer;
    private final int deckIdx;

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