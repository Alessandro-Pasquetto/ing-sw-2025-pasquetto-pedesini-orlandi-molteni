package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class BoxToDiscardMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int boxToDiscard;

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
