package org.progetto.messages.toClient.Building;

import org.progetto.server.model.events.EventCard;

import java.io.Serializable;

public class PickedEventCardMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    EventCard eventCard;


    // =======================
    // CONSTRUCTORS
    // =======================

    public PickedEventCardMessage(EventCard eventCard) {
     this.eventCard = eventCard;

    }

    // =======================
    // GETTERS
    // =======================


    public EventCard getEventCard() {
        return eventCard;
    }

    public String getImgSrc() {
        return eventCard.getImgSrc();
    }

}