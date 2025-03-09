package org.progetto.server.model.events;
import java.util.List;

public class Battlezone {

    // =======================
    // ATTRIBUTES
    // =======================

    private int penaltyDays;
    private int penaltyCrew;
    private int penaltyBoxes;

    // =======================
    // CONSTRUCTORS
    // =======================



    // =======================
    // GETTERS
    // =======================



    // =======================
    // SETTERS
    // =======================



    // =======================
    // OTHER METHODS
    // =======================

    // The Battlezone card has 3 lines that are evaluated one after the other
    // Each line provides a criterion and a penalty for the player who is the weakest for that criterion
    // Light cannon fire can only be stopped by a shield facing the right direction and activated by a battery
    // Heavy cannon fire cannot be stopped
    // Among the tied players, the one furthest ahead on the route is the only one who has to face the penalty
    public void effect() {

    }
}
