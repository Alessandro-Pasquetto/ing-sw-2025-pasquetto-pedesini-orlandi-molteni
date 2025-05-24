package org.progetto.messages.toClient.Track;

import org.progetto.server.model.Player;

import java.io.Serializable;
import java.util.ArrayList;

public class ResponseTrackMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private ArrayList<Player> travelers;
    private Player[] track;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseTrackMessage(ArrayList<Player> travelers, Player[] track) {
        this.travelers = travelers;
        this.track = track;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Player> getTravelers() {
        return travelers;
    }

    public Player[] getTrack() {
        return track;
    }
}
