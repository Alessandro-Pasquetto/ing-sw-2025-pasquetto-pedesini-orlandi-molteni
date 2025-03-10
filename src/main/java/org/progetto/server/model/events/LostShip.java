package org.progetto.server.model.events;
import java.util.List;

public class LostShip extends EventCard{

    // =======================
    // ATTRIBUTES
    // =======================

    private int penaltyCrew;
    private int rewardCredits;
    private int penaltyDays;

    // =======================
    // CONSTRUCTORS
    // =======================

    public LostShip(CardType type, String imgSrc, int penaltyCrew, int rewardCredits, int penaltyDays) {
        super(type, imgSrc);
        this.penaltyCrew = penaltyCrew;
        this.rewardCredits = rewardCredits;
        this.penaltyDays = penaltyDays;
    }

    // =======================
    // OTHER METHODS
    // =======================

    // Only for one player and the leader chooses first
    // You can give up the indicated number of crew, and also lose the indicated number of days, to obtain the indicated number of credits
    public void effect() {

    }
}
