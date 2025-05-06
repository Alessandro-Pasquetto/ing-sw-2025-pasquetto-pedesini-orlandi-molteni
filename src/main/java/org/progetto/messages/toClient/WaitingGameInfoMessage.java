package org.progetto.messages.toClient;

import org.progetto.server.model.Player;

import java.io.Serializable;
import java.util.ArrayList;

public class WaitingGameInfoMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int id;
    private int level;
    private int maxPlayers;
    private ArrayList<Player> players;

    // =======================
    // CONSTRUCTORS
    // =======================

    public WaitingGameInfoMessage(int id, int level, int maxPlayers, ArrayList<Player> players) {
        this.id = id;
        this.level = level;
        this.maxPlayers = maxPlayers;
        this.players = players;
    }

    // =======================
    // GETTERS
    // =======================

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }
}
