package org.progetto.server.connection.socket;

import org.progetto.server.connection.games.GameManager;
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
        socketWriter = new SocketWriter(this, out);
        socketListener = new SocketListener(this, in);

        SocketServer.addSocketWriter(socketWriter);

        socketWriter.start();
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
    // OTHER METHODS
    // =======================

    // Save gameManager and player to clientHandler and add socketWriter to the game
    public void initPlayerConnection(GameManager gameManager, Player player) {
        this.gameManager = gameManager;
        this.player = player;

        SocketServer.removeSocketWriter(socketWriter);
        gameManager.addSocketWriter(player, socketWriter);
    }
}