package org.progetto.messages.toServer;

import java.io.Serializable;

public class BuildSpaceshipMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int idShip;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BuildSpaceshipMessage(int idShip) {
        this.idShip = idShip;
    }

    // =======================
    // GETTERS
    // =======================

    public int getIdShip() {
        return idShip;
    }
}