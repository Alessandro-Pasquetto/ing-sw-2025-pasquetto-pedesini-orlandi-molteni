package org.progetto.messages.toClient;

import org.progetto.server.model.events.EventCard;

import java.io.Serializable;

public class PickedEventCardMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    String imgSrc;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PickedEventCardMessage(String imgSrc) {
        this.imgSrc = imgSrc;

    }

    // =======================
    // GETTERS
    // =======================

   public String getImgSrc() {
        return imgSrc;
   }

}