package org.progetto.messages.toClient.Spaceship;

import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;

import java.io.Serializable;

public class UpdateSpaceshipMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Spaceship spaceship;
    private final Player owner;

    // =======================
    // CONSTRUCTORS
    // =======================

    public UpdateSpaceshipMessage(Spaceship spaceship, Player owner) {
        this.spaceship = spaceship;
        this.owner = owner;
    }

    // =======================
    // GETTERS
    // =======================

    public Spaceship getSpaceship() {
        return spaceship;
    }

    public Player getOwner() {
        return owner;
    }
}
