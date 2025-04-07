package org.progetto.messages.toClient.Building;

import org.progetto.server.model.events.EventCard;

import java.io.Serializable;
import java.util.ArrayList;

public class PickedUpEventCardDeckMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private ArrayList<EventCard> eventCardsDeck;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PickedUpEventCardDeckMessage(ArrayList<EventCard> eventCardsDeck) {
        this.eventCardsDeck = new ArrayList<>(eventCardsDeck);
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<EventCard> getEventCardsDeck() {
        return eventCardsDeck;
    }
}
