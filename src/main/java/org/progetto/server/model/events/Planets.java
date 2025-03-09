package org.progetto.server.model.events;
import org.progetto.server.model.components.Box;
import java.util.ArrayList;

import java.util.List;

public class Planets extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private ArrayList<ArrayList<Box>> rewardsForPlanets;
    private int penaltyDays;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Planets(CardType type, String imgSrc, ArrayList<ArrayList<Box>> rewardsForPlanets, int penaltyDays) {
        super(type, imgSrc);
        this.rewardsForPlanets = rewardsForPlanets;
        this.penaltyDays = penaltyDays;
    }

    // =======================
    // OTHER METHODS
    // =======================

    // Landing on a planet costs you a certain number of penalty days. The leader chooses first
    public void effect() {

    }

    // Choose, if you want, a planet on the card
    public int choosePlanet(int idx) {
        return 0;
    }

    // Choose, if the reward boxes are more than the available spaces you have left, the boxes to keep
    public int chooseRewardsBoxes(int idx) {
        return 0;
    }
}
