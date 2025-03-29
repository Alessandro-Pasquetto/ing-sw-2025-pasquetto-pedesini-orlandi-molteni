package org.progetto.messages.toClient;

import org.progetto.server.model.events.EventCard;

import java.io.Serializable;
import java.util.ArrayList;

public class PickedUpEventCardDeck implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private ArrayList<EventCard> eventCardsDeck;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PickedUpEventCardDeck(ArrayList<EventCard> eventCardsDeck) {
        this.eventCardsDeck = new ArrayList<>(eventCardsDeck);
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<EventCard> getEventCardsDeck() {
        return eventCardsDeck;
    }
}
