package org.progetto.server.model.events;
import java.util.List;

abstract class EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private CardType type;
    private String imgSrc;

    // =======================
    // CONSTRUCTOR
    // =======================

    public EventCard(CardType type, String imgSrc) {
        this.type = type;
        this.imgSrc = imgSrc;
    }

    // =======================
    // GETTERS
    // =======================

    public CardType getType() {
        return type;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    // =======================
    // SETTERS
    // =======================

    public void setType(CardType type) {
        this.type = type;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }
}
