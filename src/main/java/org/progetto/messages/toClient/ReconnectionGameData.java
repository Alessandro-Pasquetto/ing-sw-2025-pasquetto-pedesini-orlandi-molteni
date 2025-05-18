package org.progetto.messages.toClient;

import java.io.Serializable;

public class ReconnectionGameData implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int levelGame;
    private final String gamePhase;
    private final int playerColor;
    private final String nameActivePlayer;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ReconnectionGameData(int levelGame, String gamePhase, int playerColor, String nameActivePlayer) {
        this.levelGame = levelGame;
        this.gamePhase = gamePhase;
        this.playerColor = playerColor;
        this.nameActivePlayer = nameActivePlayer;
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

    public String getNameActivePlayer() {
        return nameActivePlayer;
    }
}