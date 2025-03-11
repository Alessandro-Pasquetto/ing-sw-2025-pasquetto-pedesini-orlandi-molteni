package org.progetto.server.model.events;

import java.util.ArrayList;

public class Penalty {

    // =======================
    // ATTRIBUTES
    // =======================

    private PenaltyType type;
    private int neededAmount;
    private ArrayList<Projectile> meteors;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Penalty(PenaltyType type, int neededAmount, ArrayList<Projectile> meteors) {
        this.type = type;
        this.neededAmount = neededAmount;
        this.meteors = meteors;
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

    public ArrayList<Projectile> getMeteors() {
        return meteors;
    }


}