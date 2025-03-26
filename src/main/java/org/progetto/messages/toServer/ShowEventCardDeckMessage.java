package org.progetto.messages.toServer;

public class ShowEventCardDeckMessage {

    // =======================
    // ATTRIBUTES
    // =======================

    int idxDeck;

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