package org.progetto.messages;

import java.io.Serializable;

public class PlaceHandComponentAndPickCardMessage implements Serializable {

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
    public PlaceHandComponentAndPickCardMessage(int x, int y, int rotation, int idxDeck) {
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