package org.progetto.messages.toClient;

import org.progetto.server.model.Player;

import java.io.Serializable;
import java.util.ArrayList;

public class ResponsePlayersMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<Player> players;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponsePlayersMessage(ArrayList<Player> players) {
        this.players = players;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Player> getPlayers() {
        return players;
    }
}