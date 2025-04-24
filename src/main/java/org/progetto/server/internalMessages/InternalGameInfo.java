package org.progetto.server.internalMessages;

import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.Player;

/**
 * Internal message class to handle:
 * - Socket: save params gameCommunicationHandler and player when someone creates/joins a game
 * - RMI: adds current RMI client to GameCommunicationHandler list
 */
public class InternalGameInfo {

    // =======================
    // ATTRIBUTES
    // =======================

    private final GameManager gameManager;
    private final Player player;

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