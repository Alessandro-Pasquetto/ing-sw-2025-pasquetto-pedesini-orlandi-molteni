package org.progetto.messages.toClient;

import java.io.Serializable;

public class NotifyNewGameMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int idGame;

    // =======================
    // CONSTRUCTORS
    // =======================
    public NotifyNewGameMessage(int idGame) {
        this.idGame = idGame;
    }

    // =======================
    // GETTERS
    // =======================
    public int getIdGame() {
        return idGame;
    }
}