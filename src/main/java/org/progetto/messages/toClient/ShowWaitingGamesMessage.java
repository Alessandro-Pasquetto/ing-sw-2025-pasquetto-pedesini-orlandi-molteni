package org.progetto.messages.toClient;

import java.io.Serializable;
import java.util.ArrayList;

public class ShowWaitingGamesMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<Integer> idWaitingGames;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ShowWaitingGamesMessage(ArrayList<Integer> idWaitingGames){
        this.idWaitingGames = idWaitingGames;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Integer> getIdWaitingGames() {
        return idWaitingGames;
    }
}