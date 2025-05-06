package org.progetto.messages.toClient;

import java.io.Serializable;
import java.util.ArrayList;

public class ShowWaitingGamesMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<WaitingGameInfoMessage> waitingGames;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ShowWaitingGamesMessage(ArrayList<WaitingGameInfoMessage> waitingGames) {
        this.waitingGames = waitingGames;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<WaitingGameInfoMessage> getWaitingGames() {
        return waitingGames;
    }
}