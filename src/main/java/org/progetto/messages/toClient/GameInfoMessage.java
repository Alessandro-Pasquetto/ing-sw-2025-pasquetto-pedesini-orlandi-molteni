package org.progetto.messages.toClient;

import java.io.Serializable;

public class GameInfoMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int idGame;
    private final int levelGame;
    private final int numMaxPlayers;

    // =======================
    // CONSTRUCTORS
    // =======================

    public GameInfoMessage(int idGame, int levelGame, int numMaxPlayers) {
        this.idGame = idGame;
        this.levelGame = levelGame;
        this.numMaxPlayers = numMaxPlayers;
    }

    // =======================
    // GETTERS
    // =======================

    public int getIdGame() {
        return idGame;
    }

    public int getLevelGame() {
        return levelGame;
    }

    public int getNumMaxPlayers() {
        return numMaxPlayers;
    }
}