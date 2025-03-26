package org.progetto.server.internalMessages;

import org.progetto.server.controller.GameManager;
import org.progetto.server.model.Player;

public class InternalGameInfo {

    // =======================
    // ATTRIBUTES
    // =======================

    private GameManager gameManager;
    private Player player;

    // =======================
    // CONSTRUCTORS
    // =======================
    public InternalGameInfo(GameManager gameManager, Player player) {
        this.gameManager = gameManager;
        this.player = player;
    }

    // =======================
    // GETTERS
    // =======================

    public GameManager getGameManager() {
        return gameManager;
    }

    public Player getPlayer() {
        return player;
    }
}