package org.progetto.messages.toClient;

import java.io.Serializable;

public class CrewToDiscardMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int crewToDiscard;

    // =======================
    // CONSTRUCTORS
    // =======================

    public CrewToDiscardMessage(int crewToDiscard) {
        this.crewToDiscard = crewToDiscard;
    }

    // =======================
    // GETTERS
    // =======================

    public int getCrewToDiscard() {
        return crewToDiscard;
    }
}