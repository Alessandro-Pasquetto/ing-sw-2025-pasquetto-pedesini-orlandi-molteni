package org.progetto.server;

import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.rmi.RmiServer;
import org.progetto.server.connection.socket.SocketServer;
import org.progetto.server.controller.LobbyController;

public class MainServer {

    public static void main(String[] args) {

        SocketServer socketServer = new SocketServer();
        RmiServer rmiServer = new RmiServer();

        socketServer.start();
        rmiServer.start();

        // Set disconnection detection interval
        LobbyController.setLobbyDisconnectionDetectionInterval(5000);
        GameManager.setGameDisconnectionDetectionInterval(5000);

        LobbyController.startLobbyPinger();
    }
}