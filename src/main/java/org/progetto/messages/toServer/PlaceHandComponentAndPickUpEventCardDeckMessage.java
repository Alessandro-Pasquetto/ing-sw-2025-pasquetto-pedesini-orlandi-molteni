package org.progetto.messages.toServer;

import java.io.Serializable;

public class PlaceHandComponentAndPickUpEventCardDeckMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int x;
    private final int y;
    private final int rotation;
    private final int idxDeck;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlaceHandComponentAndPickUpEventCardDeckMessage(int x, int y, int rotation, int idxDeck) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.idxDeck = idxDeck;
    }

    // =======================
    // GETTERS
    // =======================

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRotation() {
        return rotation;
    }

    public int getIdxDeck() {
        return idxDeck;
    }
}