package org.progetto.messages.toClient.Building;

import org.progetto.server.model.events.EventCard;

import java.io.Serializable;
import java.util.ArrayList;

public class PickedUpEventCardDeckMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int deckIdx;
    private final ArrayList<EventCard> eventCardsDeck;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PickedUpEventCardDeckMessage(int deckIdx, ArrayList<EventCard> eventCardsDeck) {
        this.deckIdx = deckIdx;
        this.eventCardsDeck = new ArrayList<>(eventCardsDeck);
    }

    // =======================
    // GETTERS
    // =======================

    public int getDeckIdx() {
        return deckIdx;
    }

    public ArrayList<EventCard> getEventCardsDeck() {
        return eventCardsDeck;
    }
}
