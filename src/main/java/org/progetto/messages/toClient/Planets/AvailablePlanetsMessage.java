package org.progetto.messages.toClient.Planets;

import org.progetto.server.model.components.Box;

import java.io.Serializable;
import java.util.ArrayList;

public class AvailablePlanetsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private ArrayList<ArrayList<Box>> rewardsForPlanets;
    private final boolean[] planetsTaken;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AvailablePlanetsMessage(ArrayList<ArrayList<Box>> rewardsForPlanets, boolean[] planetsTaken) {
        this.rewardsForPlanets = rewardsForPlanets;
        this.planetsTaken = planetsTaken;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<ArrayList<Box>> getRewardsForPlanets() {
        return rewardsForPlanets;
    }

    public boolean[] getPlanetsTaken() {
        return planetsTaken;
    }
}