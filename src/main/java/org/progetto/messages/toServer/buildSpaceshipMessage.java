package org.progetto.messages.toServer;

import java.io.Serializable;

public class buildSpaceshipMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================
    int idShip;

    // =======================
    // CONSTRUCTORS
    // =======================
    public buildSpaceshipMessage(int idShip) {
        this.idShip = idShip;
    }

    // =======================
    // GETTERS
    // =======================

    public int getIdShip() {
        return idShip;
    }

}