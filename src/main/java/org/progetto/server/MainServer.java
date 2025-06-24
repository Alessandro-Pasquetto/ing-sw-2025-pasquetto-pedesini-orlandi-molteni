package org.progetto.server;

import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.rmi.RmiServer;
import org.progetto.server.connection.socket.SocketServer;
import org.progetto.server.controller.LobbyController;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class MainServer {

    public static void main(String[] args) {
        SocketServer socketServer = new SocketServer();
        RmiServer rmiServer = new RmiServer();

        socketServer.start();
        rmiServer.start();

        // Set disconnection detection intervals
        LobbyController.setLobbyDisconnectionDetectionInterval(1000);
        GameManager.setGameDisconnectionDetectionInterval(1000);

        LobbyController.startLobbyPinger();

        // Delete save files
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Path path = Paths.get("saves");

            try {
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                System.out.println("Error while deleting the directory: " + e.getMessage());
            }
        }));
    }
}