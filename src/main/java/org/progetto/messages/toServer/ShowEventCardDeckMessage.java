package org.progetto.messages.toServer;

public class ShowEventCardDeckMessage {

    // =======================
    // ATTRIBUTES
    // =======================

    private int idxDeck;

    // =======================
    // CONSTRUCTORS
    // =======================

    ShowEventCardDeckMessage(int idxDeck) {
        this.idxDeck = idxDeck;
    }

    // =======================
    // GETTERS
    // =======================

    public int getIdxDeck() {
        return idxDeck;
    }
}