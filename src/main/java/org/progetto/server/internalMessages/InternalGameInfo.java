package org.progetto.server.internalMessages;

import org.progetto.server.connection.games.GameCommunicationHandler;
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

    private GameCommunicationHandler gameCommunicationHandler;
    private Player player;

    // =======================
    // CONSTRUCTORS
    // =======================

    public InternalGameInfo(GameCommunicationHandler gameCommunicationHandler, Player player) {
        this.gameCommunicationHandler = gameCommunicationHandler;
        this.player = player;
    }

    // =======================
    // GETTERS
    // =======================

    public GameCommunicationHandler getGameManager() {
        return gameCommunicationHandler;
    }

    public Player getPlayer() {
        return player;
    }
}