package org.progetto.server.model.events;
import java.util.List;

public class OpenSpace extends EventCard {

    // =======================
    // CONSTRUCTORS
    // =======================

    public OpenSpace(CardType type, String imgSrc) {
        super(type, imgSrc);
    }

    // =======================
    // OTHER METHODS
    // =======================

    // In turn, starting with the leader and continuin in route order, each player declare their power. Immediately afterward each player advances as many spaces as their power
    public void effect() {

    }
}
