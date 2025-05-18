package org.progetto.messages.toClient.Positioning;

import org.progetto.server.model.Player;

import java.io.Serializable;
import java.util.ArrayList;

public class PlayersInPositioningDecisionOrderMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    ArrayList<Player> players;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PlayersInPositioningDecisionOrderMessage(ArrayList<Player> players) {
        this.players = players;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Player> getPlayers() {
        return players;
    }
}
