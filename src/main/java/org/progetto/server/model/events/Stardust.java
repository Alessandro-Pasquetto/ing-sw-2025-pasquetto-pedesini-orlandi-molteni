package org.progetto.server.model.events;
import java.util.List;

public class Stardust extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private int penaltyDays;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Stardust(CardType type, String imgSrc, int penaltyDays) {
        super(type, imgSrc);
        this.penaltyDays = penaltyDays;
    }

    // =======================
    // OTHER METHODS
    // =======================

    // You lose one day for each exposed connectors
    // Reverse route order
    public void effect() {

    }
}
