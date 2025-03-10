package org.progetto.server.model.events;
import org.progetto.server.model.components.Box;
import java.util.ArrayList;

public class Smugglers extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private int firePowerRequired;
    private int penaltyBoxes;
    private int penaltyDays;
    private ArrayList<Box> rewardBoxes;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Smugglers(CardType type, String imgSrc, int firePowerRequired, int penaltyBoxes, int penaltyDays, ArrayList<Box> rewardBoxes) {
        super(type, imgSrc);
        this.firePowerRequired = firePowerRequired;
        this.penaltyBoxes = penaltyBoxes;
        this.penaltyDays = penaltyDays;
        this.rewardBoxes = rewardBoxes;
    }

    // =======================
    // OTHER METHODS
    // =======================

    // Route order: the leather first
    // If the power of his ship is greater than that of the smugglers, the player wins and obtains the indicated rewardsBoxes but loses the indicated days
    // If you lose, the Smugglers take your most valuable boxes. If you run out of boxes, they take your batteries instead. Then the enemy attacks the next player
    // In case of a tie (same power) nothing happens and the enemy passes to the next player
    public void effect() {

    }

    //
    public int chooseRewardsBoxes(int idx) {
        return 0;
    }
}
