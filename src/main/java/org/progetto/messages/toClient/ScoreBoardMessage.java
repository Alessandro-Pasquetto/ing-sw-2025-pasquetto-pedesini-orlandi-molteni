package org.progetto.messages.toClient;

import org.progetto.server.model.Player;

import java.io.Serializable;
import java.util.ArrayList;

public class ScoreBoardMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private ArrayList<Player> scoreBoard;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ScoreBoardMessage(ArrayList<Player> arrivalOrderPlayers) {
        this.scoreBoard = arrivalOrderPlayers;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Player> getScoreBoard() {
        return scoreBoard;
    }
}
