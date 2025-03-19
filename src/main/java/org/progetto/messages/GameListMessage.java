package org.progetto.messages;

import java.io.Serializable;
import java.util.ArrayList;

public class GameListMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================
    ArrayList<Integer> idGames;

    // =======================
    // CONSTRUCTORS
    // =======================
    public GameListMessage(ArrayList<Integer> idGames) {
        this.idGames = idGames;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Integer> getIdGames() {
        return idGames;
    }
}