package org.progetto.messages.toServer;

import org.progetto.server.model.Player;

import java.io.Serializable;

public class RequestSpaceshipMessage implements Serializable{

    // =======================
    // ATTRIBUTES
    // =======================
    String owner;

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