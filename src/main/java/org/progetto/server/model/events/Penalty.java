package org.progetto.server.model.events;

import java.util.ArrayList;

public class Penalty {

    // =======================
    // ATTRIBUTES
    // =======================

    private PenaltyType type;
    private int neededAmount;
    private ArrayList<Projectile> shots;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Penalty(PenaltyType type, int neededAmount, ArrayList<Projectile> shots) {
        this.type = type;
        this.neededAmount = neededAmount;
        this.shots = shots;
    }

    // =======================
    // GETTERS
    // =======================

    public PenaltyType getType() {
        return type;
    }

    public int getNeededAmount() {
        return neededAmount;
    }

    public ArrayList<Projectile> getShots() {
        return shots;
    }


}