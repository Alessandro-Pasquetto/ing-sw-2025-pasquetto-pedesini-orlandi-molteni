package org.progetto.messages.toClient;

import org.progetto.server.model.Player;

import java.io.Serializable;
import java.util.ArrayList;

public class ShowPlayersMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private ArrayList<Player> players;


    // =======================
    // CONSTRUCTORS
    // =======================

    public ShowPlayersMessage(ArrayList<Player> players) {
        this.players = players;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Player> getPlayers() {
        return players;
    }

}