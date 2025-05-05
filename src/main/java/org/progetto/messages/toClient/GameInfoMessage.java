package org.progetto.messages.toClient;

import java.io.Serializable;

public class GameInfoMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int idGame;
    private int levelGame;
    private int color;
    private int numMaxPlayers;

    // =======================
    // CONSTRUCTORS
    // =======================

    public GameInfoMessage(int idGame, int levelGame, int numMaxPlayers, int color) {
        this.idGame = idGame;
        this.levelGame = levelGame;
        this.numMaxPlayers = numMaxPlayers;
        this.color = color;
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

    public int getColor() {
        return color;
    }
}