package org.progetto.messages.toClient;

import org.progetto.server.model.Spaceship;
import java.io.Serializable;

public class AskAlien implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String color;
    private final Spaceship spaceship;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AskAlien(String color, Spaceship spaceship) {
        this.color = color;
        this.spaceship = spaceship;
    }

    // =======================
    // GETTERS
    // =======================

    public String getColor() {
        return color;
    }

    public Spaceship getSpaceship() {
        return spaceship;
    }
}