package org.progetto.messages.toClient;

import org.progetto.server.connection.games.WaitingGameInfo;
import java.io.Serializable;
import java.util.ArrayList;

public class ShowWaitingGamesMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<WaitingGameInfo> waitingGames;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ShowWaitingGamesMessage(ArrayList<WaitingGameInfo> waitingGames) {
        this.waitingGames = waitingGames;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<WaitingGameInfo> getWaitingGames() {
        return waitingGames;
    }
}