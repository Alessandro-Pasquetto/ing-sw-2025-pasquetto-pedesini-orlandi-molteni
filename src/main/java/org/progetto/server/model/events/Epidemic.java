package org.progetto.server.model.events;

public class Epidemic extends EventCard {

    // =======================
    // CONSTRUCTORS
    // =======================

    public Epidemic(CardType type, String imgSrc) {
        super(type, imgSrc);
    }

    // =======================
    // OTHER METHODS
    // =======================

    // The Epidemic makes you remove 1 crew member (human or alien) from each occupied cabin that is interconnected to another occupied cabin
    public void effect() {

    }
}
