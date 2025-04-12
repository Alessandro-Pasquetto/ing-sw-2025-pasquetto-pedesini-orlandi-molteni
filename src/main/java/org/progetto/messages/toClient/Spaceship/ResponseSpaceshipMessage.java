package org.progetto.messages.toClient.Spaceship;

import org.progetto.server.model.Spaceship;

import java.io.Serializable;

public class ResponseSpaceshipMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Spaceship spaceship;
    private final String owner;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseSpaceshipMessage(Spaceship spaceship, String owner) {
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