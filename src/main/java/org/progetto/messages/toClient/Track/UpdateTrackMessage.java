package org.progetto.messages.toClient.Track;

import org.progetto.server.model.Player;

import java.io.Serializable;
import java.util.ArrayList;

public class UpdateTrackMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<Player> playersInTrack;
    private final Player[] track;

    // =======================
    // CONSTRUCTORS
    // =======================

    public UpdateTrackMessage(ArrayList<Player> playersInTrack, Player[] track) {
        this.playersInTrack = playersInTrack;
        this.track = track;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Player> getPlayersInTrack() {
        return playersInTrack;
    }

    public Player[] getTrack() {
        return track;
    }
}
