package org.progetto.messages.toServer;

import java.io.Serializable;

public class CreateGameMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================
    private final int levelGame;
    private final int numPlayers;
    private final String name;

    // =======================
    // CONSTRUCTORS
    // =======================
    public CreateGameMessage(int levelGame, int numPlayers, String name) {
        this.levelGame = levelGame;
        this.numPlayers = numPlayers;
        this.name = name;
    }

    // =======================
    // GETTERS
    // =======================
    public int getLevelGame() {
        return levelGame;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public String getName() {
        return name;
    }
}