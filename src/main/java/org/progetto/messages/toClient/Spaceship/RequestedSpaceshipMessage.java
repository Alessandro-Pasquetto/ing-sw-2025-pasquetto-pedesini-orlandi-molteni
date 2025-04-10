package org.progetto.messages.toClient.Spaceship;

import org.progetto.server.model.Spaceship;

import java.io.Serializable;

public class RequestedSpaceshipMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    Spaceship spaceship;
    String owner;

    // =======================
    // CONSTRUCTORS
    // =======================

    public RequestedSpaceshipMessage(Spaceship spaceship,String owner) {
        this.spaceship = spaceship;
        this.owner = owner;
    }

    // =======================
    // GETTERS
    // =======================
    public Spaceship getSpaceship() {
        return spaceship;
    }

    public String getOwner() {
        return owner;
    }


}