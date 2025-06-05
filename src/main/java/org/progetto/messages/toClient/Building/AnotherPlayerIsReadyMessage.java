package org.progetto.messages.toClient.Building;

import java.io.Serializable;

public class AnotherPlayerIsReadyMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String namePlayer;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerIsReadyMessage(String namePlayer) {
        this.namePlayer = namePlayer;
    }

    // =======================
    // GETTERS
    // =======================

    public String getNamePlayer() {
        return namePlayer;
    }
}
