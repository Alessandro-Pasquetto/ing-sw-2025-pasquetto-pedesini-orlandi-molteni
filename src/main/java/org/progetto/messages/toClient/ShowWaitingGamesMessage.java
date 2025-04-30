package org.progetto.messages.toClient;

import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.WaitingGameInfo;
import org.progetto.server.model.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

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