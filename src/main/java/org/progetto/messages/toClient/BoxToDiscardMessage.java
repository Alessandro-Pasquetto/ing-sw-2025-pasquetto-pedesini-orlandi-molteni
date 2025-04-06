package org.progetto.messages.toClient;

import java.io.Serializable;

public class BoxToDiscardMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int boxToDiscard;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BoxToDiscardMessage(int boxToDiscard) {
        this.boxToDiscard = boxToDiscard;
    }

    // =======================
    // GETTERS
    // =======================

    public int getBoxToDiscard() {
        return boxToDiscard;
    }
}
