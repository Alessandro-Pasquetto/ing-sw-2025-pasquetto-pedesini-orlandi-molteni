package org.progetto.server;

import org.progetto.server.connection.rmi.RmiServer;
import org.progetto.server.connection.socket.SocketServer;

public class MainServer {

    public static void main(String[] args) {

        SocketServer socketServer = new SocketServer();
        RmiServer rmiServer = new RmiServer();

        socketServer.start();
        rmiServer.start();
    }
}