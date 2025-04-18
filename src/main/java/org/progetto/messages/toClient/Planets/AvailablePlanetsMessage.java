package org.progetto.messages.toClient.Planets;

import java.io.Serializable;

public class AvailablePlanetsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================
    private final boolean[] planetsTaken;

    // =======================
    // CONSTRUCTORS
    // =======================
    public AvailablePlanetsMessage(boolean[] planetsTaken) {
        this.planetsTaken = planetsTaken;
    }

    // =======================
    // GETTERS
    // =======================
    public boolean[] getPlanetsTaken() {
        return planetsTaken;
    }


}