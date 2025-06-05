package org.progetto.messages.toClient.Spaceship;

import org.progetto.server.model.Spaceship;

import java.io.Serializable;

public class ResponseSpaceshipStatsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Spaceship spaceship;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseSpaceshipStatsMessage(Spaceship spaceship) {
        this.spaceship = spaceship;
    }

    // =======================
    // GETTERS
    // =======================

    public Spaceship getSpaceship() {
        return spaceship;
    }
}
