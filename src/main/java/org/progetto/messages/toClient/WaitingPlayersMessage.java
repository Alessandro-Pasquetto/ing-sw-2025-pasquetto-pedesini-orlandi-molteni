package org.progetto.messages.toClient;

import org.progetto.server.model.Player;
import java.io.Serializable;
import java.util.ArrayList;

public class WaitingPlayersMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<Player> players = new ArrayList<Player>();

    // =======================
    // CONSTRUCTORS
    // =======================

    public WaitingPlayersMessage(ArrayList<Player> players) {
        this.players.addAll(players);
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Player> getPlayers() {
        return players;
    }
}