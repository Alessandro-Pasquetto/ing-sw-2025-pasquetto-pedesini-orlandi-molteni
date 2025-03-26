package org.progetto.messages.toServer;

import java.io.Serializable;

public class PlaceHandComponentAndShowEventCardDeckMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================
    int x;
    int y;
    int rotation;
    int idxDeck;

    // =======================
    // CONSTRUCTORS
    // =======================
    public PlaceHandComponentAndShowEventCardDeckMessage(int x, int y, int rotation, int idxDeck) {
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