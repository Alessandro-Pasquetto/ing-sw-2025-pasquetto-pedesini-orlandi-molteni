package org.progetto.messages.toClient.Positioning;

import org.progetto.server.model.Player;

import java.io.Serializable;
import java.util.ArrayList;

public class ShowPlayersInPositioningDecisionOrderMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    ArrayList<Player> players;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ShowPlayersInPositioningDecisionOrderMessage(ArrayList<Player> players) {
        this.players = players;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Player> getPlayers() {
        return players;
    }
}
