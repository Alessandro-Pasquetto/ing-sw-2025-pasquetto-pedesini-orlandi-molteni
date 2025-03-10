package org.progetto.server.model.events;
import org.progetto.server.model.components.Box;
import java.util.ArrayList;

public class LostStation extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private int requiredCrew;
    private ArrayList<Box> rewardBoxes;
    private int penaltyDays;

    // =======================
    // CONSTRUCTORS
    // =======================

    public LostStation(CardType type, String imgSrc, int requiredCrew, ArrayList<Box> rewardBoxes, int penaltyDays) {
        super(type, imgSrc);
        this.requiredCrew = requiredCrew;
        this.rewardBoxes = rewardBoxes;
        this.penaltyDays = penaltyDays;
    }

    // =======================
    // OTHER METHODS
    // =======================

    // Choose, if the reward boxes are more than the available spaces you have left, the boxes to keep
    public int chooseRewardsBoxes(int idx) {
        return 0;
    }

    // Only for one player and the leader chooses first
    // You must have at least the same number of humans and aliens indicated to obtain the indicated boxes
    // You also lose the indicated number of days
    public void effect() {

    }
}
