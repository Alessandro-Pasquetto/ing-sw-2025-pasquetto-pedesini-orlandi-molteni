package org.progetto.server.model.events;
import java.io.Serializable;
import java.util.List;

public abstract class EventCard implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private CardType type;
    private String imgSrc;
    private int level;

    // =======================
    // CONSTRUCTOR
    // =======================

    public EventCard(CardType type, int level,String imgSrc) {
        this.type = type;
        this.level = level;
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

    public int getLevel() {
        return level;
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
