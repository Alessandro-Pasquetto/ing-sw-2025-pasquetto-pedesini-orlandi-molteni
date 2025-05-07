package org.progetto.server.connection.socket;

import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.LobbyController;
import org.progetto.server.model.Player;
import java.io.*;

/**
 * Stores socket client data
 */
public class ClientHandler {

    // =======================
    // ATTRIBUTES
    // =======================

    private GameManager gameManager;
    private Player player;
    private final SocketListener socketListener;
    private final SocketWriter socketWriter;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ClientHandler(ObjectOutputStream out, ObjectInputStream in) {
        gameManager = null;
        player = null;
        socketWriter = new SocketWriter(out);
        socketListener = new SocketListener(this, in);

        LobbyController.addSender(socketWriter);

        socketListener.start();
    }

    // =======================
    // GETTERS
    // =======================

    public GameManager getGameManager() {
        return gameManager;
    }

    public SocketWriter getSocketWriter() {
        return socketWriter;
    }

    public Player getPlayer() {
        return player;
    }

    // =======================
    // SETTERS
    // =======================

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}