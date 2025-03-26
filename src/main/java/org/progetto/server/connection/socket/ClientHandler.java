package org.progetto.server.connection.socket;


import org.progetto.server.controller.GameController;
import org.progetto.server.model.Player;

import java.io.*;

public class ClientHandler {

    // =======================
    // ATTRIBUTES
    // =======================
    private GameController gameController;
    private Player player;
    private SocketListener socketListener;
    private SocketWriter socketWriter;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ClientHandler(ObjectOutputStream out, ObjectInputStream in) {

        gameController = null;
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

    public GameController getGameController() {
        return gameController;
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
    public void setGameControllerAndJoin(GameController gameController, Player player) {
        this.gameController = gameController;
        this.player = player;

        SocketServer.removeSocketWriter(socketWriter);
        gameController.addSocketWriter(socketWriter);
        gameController.getGame().addPlayer(player);
    }
}