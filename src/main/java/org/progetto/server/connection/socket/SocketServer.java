package org.progetto.server.connection.socket;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketServer{

    // =======================
    // ATTRIBUTES
    // =======================

    private static final List<SocketWriter> socketWriters = new ArrayList<>();
    private static final AtomicInteger currentIdGame = new AtomicInteger(0);

    // =======================
    // MAIN
    // =======================

    public static void main(String[] args) {
        try{
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("SocketServer listening on port 8080...");

            while (true) {
                Socket clientSocket = serverSocket.accept();

                new ClientHandler(new ObjectOutputStream(clientSocket.getOutputStream()), new ObjectInputStream(clientSocket.getInputStream()));
            }

        } catch (IOException e) {
            System.err.println("Errore durante l'avvio del server: " + e.getMessage());
        }
    }

    // =======================
    // GETTERS
    // =======================

    public static int getCurrentIdGameAndIncrement() {
        return currentIdGame.getAndIncrement();
    }

    // =======================
    // OTHER METHODS
    // =======================

    public static void addSocketWriter(SocketWriter socketWriter){
        synchronized (socketWriters){
            socketWriters.add(socketWriter);
        }
    }

    public static void removeSocketWriter(SocketWriter socketWriter){
        synchronized (socketWriters){
            socketWriters.remove(socketWriter);
        }
    }

    public static void broadcastMessage(Object messageObj) {
        ArrayList<SocketWriter> socketWritersCopy;

        synchronized (socketWriters) {
            socketWritersCopy = new ArrayList<>(socketWriters);
        }

        for (SocketWriter sw : socketWritersCopy) {
            sw.sendMessage(messageObj);
        }
    }
}
