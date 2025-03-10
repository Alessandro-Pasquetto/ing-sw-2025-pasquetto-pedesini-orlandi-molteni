package org.progetto.server.model.events;
import org.progetto.server.model.components.Box;
import java.util.ArrayList;

public class Slavers extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private int firePowerRequired;
    private int penaltyCrew;
    private int penaltyDays;
    private int rewardCredits;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Slavers(CardType type, String imgSrc, int firePowerRequired, int penaltyCrew, int penaltyDays, int rewardCredits) {
        super(type, imgSrc);
        this.firePowerRequired = firePowerRequired;
        this.penaltyCrew = penaltyCrew;
        this.penaltyDays = penaltyDays;
        this.rewardCredits = rewardCredits;
    }

    // =======================
    // OTHER METHODS
    // =======================

    // Route order: the leather first
    // If the power of his ship is greater than that of the slavers, the player wins and obtains the indicated credits but loses the indicated days
    // If you lose, the Slavers take the indicated number of your crew (you can choose who between humans and aliens)
    // In case of a tie (same power) nothing happens and the enemy passes to the next player
    public void effect() {

    }
}
