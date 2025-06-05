package org.progetto.messages.toClient.Building;

import java.io.Serializable;

public class AnotherPlayerDiscardComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String namePlayer;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerDiscardComponentMessage(String namePlayer) {
        this.namePlayer = namePlayer;
    }

    // =======================
    // GETTERS
    // =======================

    public String getNamePlayer() {
        return namePlayer;
    }
}