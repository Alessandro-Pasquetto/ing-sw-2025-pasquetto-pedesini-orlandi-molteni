package org.progetto.messages.toClient;

import java.io.Serializable;

public class ReconnectionGameData implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int levelGame; //todo aggiungere dati necessari
    private int playerColor;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ReconnectionGameData(int levelGame, int playerColor) {
        this.levelGame = levelGame;
        this.playerColor = playerColor;
    }

    // =======================
    // GETTERS
    // =======================

    public int getLevelGame() {
        return levelGame;
    }

    public int getPlayerColor() {
        return playerColor;
    }
}