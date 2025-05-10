package org.progetto.messages.toServer;

import java.io.Serializable;

public class ReconnectMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int idGame;
    private final String namePlayer;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ReconnectMessage(int idGame, String namePlayer) {
        this.idGame = idGame;
        this.namePlayer = namePlayer;
    }

    // =======================
    // GETTERS
    // =======================

    public int getIdGame() {
        return idGame;
    }

    public String getNamePlayer() {
        return namePlayer;
    }
}