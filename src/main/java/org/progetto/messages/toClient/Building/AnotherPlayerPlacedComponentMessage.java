package org.progetto.messages.toClient.Building;

import org.progetto.server.model.components.Component;
import java.io.Serializable;

public class AnotherPlayerPlacedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String namePlayer;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerPlacedComponentMessage(String namePlayer) {
        this.namePlayer = namePlayer;
    }


    // =======================
    // GETTERS
    // =======================

    public String getNamePlayer() {
        return namePlayer;
    }
}