package org.progetto.server.controller;

import org.progetto.messages.NotifyNewGameMessage;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketServer{

    // =======================
    // ATTRIBUTES
    // =======================

    private static List<SocketWriter> socketWriters = new ArrayList<>();
    private static AtomicInteger currentIdGame = new AtomicInteger(0);

    // =======================
    // MAIN
    // =======================

    public static void main(String[] args) {
        try{
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("Server in ascolto sulla porta 8080...");

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

    public static void notifyNewGame(int idGame){
        synchronized (socketWriters) {
            for (SocketWriter sk : socketWriters) {
                sk.sendMessage(new NotifyNewGameMessage(idGame));
            }
        }
    }

    public static void loadGameList() {
        /*
        for (int i = 0; i < GameControllersQueue.getNumWaitingGames(); i++) {

            GameControllersQueue.getGameController(i).getGame().;
        }
         */
    }
}
