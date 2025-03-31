package org.progetto.server.connection.socket;

import org.progetto.server.connection.games.GameCommunicationHandler;
import org.progetto.server.model.Player;
import java.io.*;

/**
 * Stores socket client data
 */
public class ClientHandler {

    // =======================
    // ATTRIBUTES
    // =======================

    private GameCommunicationHandler gameCommunicationHandler;
    private Player player;
    private final SocketListener socketListener;
    private final SocketWriter socketWriter;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ClientHandler(ObjectOutputStream out, ObjectInputStream in) {
        gameCommunicationHandler = null;
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

    public GameCommunicationHandler getGameManager() {
        return gameCommunicationHandler;
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
    public void initPlayerConnection(GameCommunicationHandler gameCommunicationHandler, Player player) {
        this.gameCommunicationHandler = gameCommunicationHandler;
        this.player = player;

        SocketServer.removeSocketWriter(socketWriter);
        gameCommunicationHandler.addSocketWriter(player, socketWriter);
    }
}