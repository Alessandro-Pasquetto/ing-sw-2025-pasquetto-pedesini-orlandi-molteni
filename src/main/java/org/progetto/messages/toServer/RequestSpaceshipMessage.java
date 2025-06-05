package org.progetto.messages.toServer;

import java.io.Serializable;

public class RequestSpaceshipMessage implements Serializable{

    // =======================
    // ATTRIBUTES
    // =======================
    private final String owner;

    // =======================
    // CONSTRUCTORS
    // =======================

    public RequestSpaceshipMessage(String owner) {
        this.owner = owner;
    }

    // =======================
    // GETTERS
    // =======================

    public String getOwner() {
        return owner;
    }

}