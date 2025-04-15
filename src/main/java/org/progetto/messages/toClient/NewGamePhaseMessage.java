package org.progetto.messages.toClient;

import java.io.Serializable;

public class NewGamePhaseMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String phaseGame;

    // =======================
    // CONSTRUCTORS
    // =======================

    public NewGamePhaseMessage(String phaseGame) {
        this.phaseGame = phaseGame;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPhaseGame() {
        return phaseGame;
    }
}
