package org.progetto.messages.toClient;

import java.io.Serializable;

public class ReconnectionGameData implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int levelGame; //todo aggiungere dati necessari
    private String gamePhase;
    private int playerColor;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ReconnectionGameData(int levelGame, String gamePhase, int playerColor) {
        this.levelGame = levelGame;
        this.gamePhase = gamePhase;
        this.playerColor = playerColor;
    }

    // =======================
    // GETTERS
    // =======================

    public int getLevelGame() {
        return levelGame;
    }

    public String getGamePhase() {
        return gamePhase;
    }

    public int getPlayerColor() {
        return playerColor;
    }
}