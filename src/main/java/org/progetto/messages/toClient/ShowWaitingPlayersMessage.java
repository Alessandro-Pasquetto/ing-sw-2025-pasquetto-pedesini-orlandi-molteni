package org.progetto.messages.toClient;

import org.progetto.server.model.Player;
import java.io.Serializable;
import java.util.ArrayList;

public class ShowWaitingPlayersMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<Player> players = new ArrayList<Player>();

    // =======================
    // CONSTRUCTORS
    // =======================

    public ShowWaitingPlayersMessage(ArrayList<Player> players) {
        this.players.addAll(players);
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Player> getPlayers() {
        return players;
    }
}