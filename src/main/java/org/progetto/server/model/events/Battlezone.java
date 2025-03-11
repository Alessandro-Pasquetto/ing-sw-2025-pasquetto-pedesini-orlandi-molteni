package org.progetto.server.model.events;

import java.util.ArrayList;
import java.util.List;

public class Battlezone {

    // =======================
    // ATTRIBUTES
    // =======================

    private ArrayList<ConditionPenalty> pair;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Battlezone(ArrayList<ConditionPenalty> pair) {
        this.pair = pair;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<ConditionPenalty> getPair() {
        return pair;
    }

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
