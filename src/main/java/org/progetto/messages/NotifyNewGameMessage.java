package org.progetto.messages;

import java.io.Serializable;

public class NotifyNewGameMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================
    int idGame;

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