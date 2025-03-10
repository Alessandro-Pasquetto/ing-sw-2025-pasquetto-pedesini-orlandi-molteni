package org.progetto.server.model.events;
import java.util.ArrayList;
import java.util.List;

public class Pirates extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private int firePowerRequired;
    private int penaltyDays;
    private int rewardCredits;
    private ArrayList<Projectile> shots;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Pirates(CardType type, String imgSrc, int firePowerRequired, int penaltyDays, int rewardCredits, ArrayList<Projectile> shots) {
        super(type, imgSrc);
        this.firePowerRequired = firePowerRequired;
        this.penaltyDays = penaltyDays;
        this.rewardCredits = rewardCredits;
        this.shots = shots;
    }

    // =======================
    // OTHER METHODS
    // =======================

    // Route order: the leather first
    // If the power of his ship is greater than that of the slavers, the player wins and obtains the indicated credits but loses the indicated days
    // In case of a tie (same power) nothing happens and the enemy passes to the next player
    // If the Pirates defeat you, your ship is subjected to enemy fire (the card indicates the power and direction of the cannon shots)
    // Keep track of all the defeated players and then have the first defeated player roll two dice to determine the row or column of each hit
    // This roll applies to all the defeated players
    // Light cannon fire can only be stopped by a shield facing the right direction and activated by a battery
    // Heavy cannon fire cannot be stopped
    public void effect() {

    }
}
