package org.progetto.messages.toClient;

import org.progetto.server.model.Player;

import java.io.Serializable;
import java.util.ArrayList;

public class UpdateTravelersMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<Player> travelers;

    // =======================
    // CONSTRUCTORS
    // =======================

    public UpdateTravelersMessage(ArrayList<Player> travelers) {
        this.travelers = travelers;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Player> getTravelers() {
        return travelers;
    }
}
