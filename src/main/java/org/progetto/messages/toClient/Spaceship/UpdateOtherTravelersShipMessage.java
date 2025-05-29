package org.progetto.messages.toClient.Spaceship;

import org.progetto.server.model.Player;

import java.io.Serializable;
import java.util.ArrayList;

public class UpdateOtherTravelersShipMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<Player> travelers;

    // =======================
    // CONSTRUCTORS
    // =======================

    public UpdateOtherTravelersShipMessage(ArrayList<Player> travelers) {
        this.travelers = travelers;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Player> getTravelers() {
        return travelers;
    }
}
