package org.progetto.messages.toServer;

import java.io.Serializable;

public class JoinGameMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================
    private int idGame;
    private String name;

    // =======================
    // CONSTRUCTORS
    // =======================
    public JoinGameMessage(int idGame, String name) {
        this.idGame = idGame;
        this.name = name;
    }

    // =======================
    // GETTERS
    // =======================
    public int getIdGame() {
        return idGame;
    }

    public String getName() {
        return name;
    }
}