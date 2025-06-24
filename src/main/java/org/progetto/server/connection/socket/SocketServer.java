package org.progetto.server.connection.socket;

import java.io.*;
import java.net.*;


public class SocketServer extends Thread {

    public SocketServer() {
        this.setName("SocketServerThread");
    }

    @Override
    public void run() {
        try{
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("SocketServer listening on port 8080...");

            while(true) {

                try{
                    Socket clientSocket = serverSocket.accept();

                    new ClientHandler(new ObjectOutputStream(clientSocket.getOutputStream()), new ObjectInputStream(clientSocket.getInputStream()));
                }catch (Exception e){
                    // todo: sistemare errore quando client fa la connessione di prova per vedere se è davvero un socektServer, il clientHandler della connessione di prova è già automaticamente distrutto
                    System.out.println("Error socket server: " + e.toString());
                }
            }
        } catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
    }
}