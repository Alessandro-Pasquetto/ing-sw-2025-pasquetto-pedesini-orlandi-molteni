package org.progetto.messages.toClient;

import java.io.Serializable;
import java.util.ArrayList;

public class ShowWaitingGamesMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    ArrayList<Integer> idWaitingGames = new ArrayList<Integer>();

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